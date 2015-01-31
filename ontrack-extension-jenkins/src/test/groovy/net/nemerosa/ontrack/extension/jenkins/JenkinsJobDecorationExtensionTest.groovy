package net.nemerosa.ontrack.extension.jenkins

import net.nemerosa.ontrack.extension.jenkins.client.*
import net.nemerosa.ontrack.model.structure.*
import org.junit.Before
import org.junit.Test

import static net.nemerosa.ontrack.model.structure.NameDescription.nd
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class JenkinsJobDecorationExtensionTest {

    PropertyService propertyService
    JenkinsClientFactory jenkinsClientFactory
    JenkinsJobDecorationExtension extension
    Branch branch
    Property<JenkinsJobProperty> jenkinsJobProperty

    @Before
    public void before() {
        propertyService = mock(PropertyService)
        jenkinsClientFactory = mock(JenkinsClientFactory)
        extension = new JenkinsJobDecorationExtension(
                new JenkinsExtensionFeature(),
                propertyService,
                jenkinsClientFactory
        )

        def jenkinsConfiguration = new JenkinsConfiguration('Jenkins', 'http://jenkins', '', '')

        JenkinsClient client = mock(JenkinsClient)
        when(client.getJob('MyBuild', false)).thenReturn(
                new JenkinsJob(
                        'MyBuild',
                        'http://jenkins/MyBuild',
                        JenkinsJobResult.SUCCESS,
                        JenkinsJobState.IDLE,
                        [],
                        null
                )
        )
        when(jenkinsClientFactory.getClient(jenkinsConfiguration)).thenReturn(client)

        branch = Branch.of(
                Project.of(nd('P', '')),
                nd('B', '')
        )


        jenkinsJobProperty = Property.of(
                new JenkinsJobPropertyType(null),
                new JenkinsJobProperty(
                        jenkinsConfiguration,
                        'MyBuild'
                )
        )
    }

    @Test
    void 'Decoration for a branch'() {
        when(propertyService.getProperty(branch, JenkinsJobPropertyType.class.getName())).thenReturn(
                jenkinsJobProperty
        )
        def decoration = extension.getDecoration(branch)
        assert decoration != null
        assert decoration.decorationType == 'net.nemerosa.ontrack.extension.jenkins.JenkinsJobDecorationExtension'
        assert decoration.id == 'idle'
        assert decoration.name == null
        assert decoration.title == 'The Jenkins Job is not running.'
    }

    @Test
    void 'No decoration for a branch template'() {
        branch = branch.withType(BranchType.TEMPLATE_DEFINITION)
        when(propertyService.getProperty(branch, JenkinsJobPropertyType.class.getName())).thenReturn(
                jenkinsJobProperty
        )
        def decoration = extension.getDecoration(branch)
        assert decoration == null
    }

}
