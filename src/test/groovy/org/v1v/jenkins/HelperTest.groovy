//
// Generated from archetype; please customize.
//

package org.v1v.jenkins

import org.v1v.jenkins.Helper
import org.v1v.jenkins.Example

/**
 * Tests for the {@link Helper} class.
 */
class HelperTest
    extends GroovyTestCase
{
    void testHelp() {
        new Helper().help(new Example())
    }
}
