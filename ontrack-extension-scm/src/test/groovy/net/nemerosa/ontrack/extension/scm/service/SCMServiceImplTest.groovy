package net.nemerosa.ontrack.extension.scm.service

import org.junit.Test

class SCMServiceImplTest {

    private SCMServiceImpl service = new SCMServiceImpl(structureService)

    @Test
    void 'One pattern'() {
        def filter = service.getPathFilter(['**/*.java'])
        assert filter.test('/root/package/File.java')
        assert !filter.test('/root/package/File.groovy')
        assert !filter.test('/root/package/File.sql')
    }

    @Test
    void 'Two patterns'() {
        def filter = service.getPathFilter(['**/*.java', '**/*.groovy'])
        assert filter.test('/root/package/File.java')
        assert filter.test('/root/package/File.groovy')
        assert !filter.test('/root/package/File.sql')
    }

    @Test
    void 'No pattern'() {
        def filter = service.getPathFilter([])
        assert filter.test('/root/package/File.java')
        assert filter.test('/root/package/File.groovy')
        assert filter.test('/root/package/File.sql')
    }

}
