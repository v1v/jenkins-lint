import hudson.model.*
import hudson.triggers.*

// list of jobs
jobs = Hudson.instance?.items

// RULES
RULE_JOB_NAME    = "[JL-000] Job name"
RULE_LOG_ROTATOR = "[JL-001] Log Rotator doesn't exist"
RULE_DESCRITION  = "[JL-002] Description hasn't been set"
RULE_SCM	       = "[JL-003] SCM hasn't been set"
RULE_SCM_TRIGGER = "[JL-004] SCM trigger is polling rather than pulling"
RULE_SCM_DUPLICATED_TRIGGER = "[JL-005] SCM trigger is duplicated"
RULE_LABELS 	 = "[JL-006] Restric executions"
RULE_CLEANUP 	 = "[JL-007] CleanUp Workspace"
RULE_JAVADOC 	 = "[JL-008] Javadoc"
RULE_ARTIFACT 	 = "[JL-009] Artifact archiver"
RULE_HARCODED_SCRIPT = "[JL-010] Harcoded Script over 3 lines"
RULE_MVN_JOB_TYPE = "[JL-011] Maven Job Type" //http://www.slideshare.net/andrewbayer/seven-habits-of-highly-effective-JL-users-2014-edition
RULE_SCM_GIT_SHALLOW = "[JL-012] Git shallow"
RULE_MULTIJOB_TYPE = "[JL-013] Multijob"

// SEVERITY
HIGH = "High"
MEDIUM = "Medium"
LOW = "Low"
IGNORED = "Ignored"

severity = [:]
severity[RULE_JOB_NAME] = HIGH
severity[RULE_LOG_ROTATOR] = HIGH
severity[RULE_DESCRITION] = HIGH
severity[RULE_SCM] = HIGH
severity[RULE_SCM_TRIGGER] = HIGH
severity[RULE_SCM_DUPLICATED_TRIGGER] = HIGH
severity[RULE_LABELS] = HIGH
severity[RULE_CLEANUP] = HIGH
severity[RULE_JAVADOC] = HIGH
severity[RULE_ARTIFACT] = HIGH
severity[RULE_HARCODED_SCRIPT] = HIGH
severity[RULE_MVN_JOB_TYPE] = HIGH
severity[RULE_SCM_GIT_SHALLOW] = HIGH

/**
 * Print rule with provided rule and jobName.
 *
 * @param ruleSeverity Rule severity.
 * @param ruleDescription Text to be included in the header.
 * @param jobName Name of the Job.
 */
def printRule(ruleSeverity, ruleDescription, jobName) {
   println "\t$ruleSeverity|$ruleDescription $jobName"
}

/**
 * Return rule id.
 *
 * @param ruleDescription Rule description.
 * @return String indicating ruleId.
 */
def getRuleId(ruleDescription){
    myRegularExpression = /\[(JL-\d+)\] .+/
    matcher = ( ruleDescription =~ myRegularExpression )

    if (matcher.matches()) {
      matcher[0][1]
    }
}

/**
 * Return if ruleId has been ignored in the job description.
 *
 * @param ruleId Rule id.
 * @param jobDescription Job description.
 * @return Boolean whether that ruleId has been ignored or not.
 */
def isIgnored(ruleId, jobDescription){
  if (ruleId != null && jobDescription != null) {
    if (jobDescription.indexOf("lint:ignored:$ruleId") > -1) {
      return true
    }
    return false
  }
  return false
}

/**
 * Return if ruleId has been ignored in the job description.
 *
 * @param ruleDescription Rule description.
 * @param jobName Job name.
 * @param jobDescription Job description.
 * @return Boolean whether that ruleId has been ignored or not.
 */
def runRule(ruleDescription, jobName, jobDescription) {
  if (jobName != null && ruleDescription != null) {
    if (!isIgnored(getRuleId(ruleDescription),jobDescription)){
      printRule (severity[ruleDescription], ruleDescription, jobName)
    }
  }
}

// RULES
jobs?.findAll{ !it.logRotator && !it.disabled }.each {
	runRule (RULE_LOG_ROTATOR, it.name, it.description)
}

jobs?.findAll{ !it.description && !it.disabled }.each {
  runRule (RULE_DESCRITION, it.name, it.description)
}

jobs?.findAll{ it.scm instanceof hudson.scm.NullSCM && !it.disabled }.each {
  runRule (RULE_SCM, it.name, it.description)
}

jobs?.findAll{ !it.disabled && it.name.contains(" ") }.each {
  	runRule (RULE_JOB_NAME, it.name, it.description)
}

TriggerDescriptor SCM_TRIGGER_DESCRIPTOR = Hudson.instance.getDescriptorOrDie(SCMTrigger.class)
assert SCM_TRIGGER_DESCRIPTOR != null;

jobs?.findAll{ it.triggers.get(SCM_TRIGGER_DESCRIPTOR) && it.triggers.get(SCM_TRIGGER_DESCRIPTOR) instanceof SCMTrigger && !it.disabled }.each {
  	runRule (RULE_SCM_TRIGGER, it.name, it.description)
}

TriggerDescriptor SCM_TIMER_TRIGGER_DESCRIPTOR = Hudson.instance.getDescriptorOrDie(TimerTrigger.class)
assert SCM_TIMER_TRIGGER_DESCRIPTOR != null;

jobs?.findAll{ it.triggers.get(SCM_TIMER_TRIGGER_DESCRIPTOR) && it.triggers.get(SCM_TIMER_TRIGGER_DESCRIPTOR) instanceof TimerTrigger && 
               it.triggers.get(SCM_TRIGGER_DESCRIPTOR) && it.triggers.get(SCM_TRIGGER_DESCRIPTOR) instanceof SCMTrigger && 
               !it.disabled }.each {
    runRule (RULE_SCM_DUPLICATED_TRIGGER, it.name, it.description)
}

jobs?.findAll{ !it.disabled && !it.getAssignedLabelString() }.each {
    runRule (RULE_LABELS, it.name, it.description)
}

// PUBLISHERS
jobs?.findAll{ !it.disabled }.each {
  def hasCleanup = false
  def hasJavadoc = true
  def hasArtifact = true

  if ( ! it.publishersList ) {
      hasCleanup = false
  }
  else {
      for(p in it.getPublishersList()) {
        if(p instanceof hudson.plugins.ws_cleanup.WsCleanup) {
            hasCleanup = true
        }
        if(p instanceof hudson.tasks.JavadocArchiver && !p.javadocDir) {
            hasJavadoc = false
        }
        if(p instanceof hudson.tasks.ArtifactArchiver && !p.artifacts) {
            hasArtifact = false
        }
      }
  }
  if (!hasCleanup) {     runRule (RULE_CLEANUP, it.name, it.description) }
  if (!hasJavadoc) {     runRule (RULE_JAVADOC, it.name, it.description) }
  if (!hasArtifact) {    runRule (RULE_ARTIFACT, it.name, it.description) }
}

// BUILDERS
jobs?.findAll{ !it.disabled && !it instanceof hudson.maven.MavenModuleSet && it.builders}.each {
  for(p in it.builders) {
    if (( p instanceof hudson.tasks.Shell || p instanceof hudson.tasks.BatchFile ) && p.getContents().split("\r\n|\r|\n").length > 3) {
      runRule (RULE_HARCODED_SCRIPT, it.name, it.description)
    }
  }
}

jobs?.findAll{ !it.disabled && it instanceof hudson.maven.MavenModuleSet}.each {
  runRule (RULE_MVN_JOB_TYPE, it.name, it.description)
}

// GIT SCM
jobs?.findAll{ it.scm && it.scm instanceof hudson.plugins.git.GitSCM && it.scm.getExtensions() && !it.disabled }.each {
  for (p in it.scm.getExtensions()) {
    if (p instanceof hudson.plugins.git.extensions.impl.CloneOption && !p.isShallow()){
  		runRule (RULE_SCM_GIT_SHALLOW, it.name, it.description)
    }
  }
}

jobs?.findAll{ !it.disabled && it instanceof com.tikal.jenkins.plugins.multijob.MultiJobProject && 
               (!it.getAssignedLabelString() || !it.getAssignedLabelString().contains("multijob")) }.each {
  runRule (RULE_MULTIJOB_TYPE, it.name, it.description)
}