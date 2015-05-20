Jenkins Lint
============

The Jenkins Lint is made up of a Jenkins Groovy script which queries the Jenkins API to validate those best practices.

Background
----------

Basic Usage
-----

1. Create a Jenkins Job using the Free-style project style to run your Jenkins Lint. This is called a "Jenkins-Lint" job.
2. Configure the Jenkins-Lint job, by adding a "Build Step" of type "Execute system Groovy Script" and paste in the body of the script/jenkins-lint.groovy
3. Publish JUnit test result reports "jenkins-lint.junit"
4. Run the job to generate your new reports from your script (YOUR_JOB_URL/ws/jenkins-lint-html-reports/overview.html)

Authors
-------
Victor Martinez <VictorMartinezRubio@gmail.com>

Thanks
------

Thanks to https://github.com/jenkinsci/cucumber-reports-plugin for having an awesome html report
Thanks to https://github.com/jenkinsci for being an awesome tool!!

License
-------
Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
“AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.