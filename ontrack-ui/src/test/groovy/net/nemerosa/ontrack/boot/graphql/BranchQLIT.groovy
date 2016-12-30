package net.nemerosa.ontrack.boot.graphql

import graphql.GraphQLException
import net.nemerosa.ontrack.extension.api.support.TestDecorationData
import net.nemerosa.ontrack.extension.api.support.TestDecorator
import net.nemerosa.ontrack.extension.api.support.TestDecoratorPropertyType
import net.nemerosa.ontrack.model.structure.NameDescription
import org.junit.Test

class BranchQLIT extends AbstractQLITSupport {

    @Test
    void 'Branch links'() {
        def branch = doCreateBranch()

        def data = run("""{branches (id: ${branch.id}) { name links { _page } } }""")
        assert data.branches.first().name == branch.name
        assert data.branches.first().links._page == "urn:test:#:entity:BRANCH:${branch.id}"
    }

    @Test
    void 'Branch by ID'() {
        def branch = doCreateBranch()

        def data = run("""{branches (id: ${branch.id}) { name } }""")
        assert data.branches.name == [branch.name]
    }

    @Test(expected = GraphQLException)
    void 'Branch by ID and project is not allowed'() {
        run("""{branches (id: 1, project: "test") { name } }""")
    }

    @Test
    void 'Branch by project'() {
        def project = doCreateProject()
        doCreateBranch(project, NameDescription.nd("B1", ""))
        doCreateBranch(project, NameDescription.nd("B2", ""))

        def data = run("""{branches (project: "${project.name}") { name } }""")
        assert data.branches.name == ['B1', 'B2']
    }

    @Test
    void 'Branch by project and name'() {
        def project = doCreateProject()
        doCreateBranch(project, NameDescription.nd("B1", ""))
        doCreateBranch(project, NameDescription.nd("B2", ""))
        doCreateBranch(project, NameDescription.nd("C1", ""))

        def data = run("""{branches (project: "${project.name}", name: "C.*") { name } }""")
        assert data.branches.name == ['C1']
    }

    @Test
    void 'Branch by name'() {
        def p1 = doCreateProject()
        def b1 = doCreateBranch(p1, NameDescription.nd("B1", ""))
        doCreateBranch(p1, NameDescription.nd("B2", ""))
        def p2 = doCreateProject()
        def b2 = doCreateBranch(p2, NameDescription.nd("B1", ""))

        def data = run("""{branches (name: "B1") { id } }""")
        assert data.branches.id == [b1.id(), b2.id()]
    }

    @Test
    void 'Branch signature'() {
        def branch = doCreateBranch()
        def data = run("""{branches (id: ${branch.id}) { creation { user time } } }""")
        assert data.branches.first().creation.user == 'user'
        assert data.branches.first().creation.time.charAt(10) == 'T'
    }

    @Test
    void 'Branch without decorations'() {
        def branch = doCreateBranch()
        def data = run("""{
                branches (id: ${branch.id}) {
                    decorations {
                        decorationType
                        data
                        error
                    }
                }   
            }""")
        def decorations = data.branches.first().decorations
        assert decorations != null
        assert decorations.empty
    }

    @Test
    void 'Branch with decorations'() {
        def branch = doCreateBranch()
        setProperty branch, TestDecoratorPropertyType, new TestDecorationData("XXX", true)

        def data = run("""{
                branches (id: ${branch.id}) {
                    decorations {
                        decorationType
                        data
                        error
                    }
                }   
            }""")

        def decorations = data.branches.first().decorations
        assert decorations != null
        assert decorations.size() == 1
        def decoration = decorations.first()
        assert decoration.decorationType == TestDecorator.class.name
        assert decoration.data.value.asText() == 'XXX'

    }

    @Test
    void 'Branch with filtered decorations and match'() {
        def branch = doCreateBranch()
        setProperty branch, TestDecoratorPropertyType, new TestDecorationData("XXX", true)

        def data = run("""{
                branches (id: ${branch.id}) {
                    decorations(type: "${TestDecorator.class.name}") {
                        decorationType
                        data
                        error
                    }
                }   
            }""")

        def decorations = data.branches.first().decorations
        assert decorations != null
        assert decorations.size() == 1
        def decoration = decorations.first()
        assert decoration.decorationType == TestDecorator.class.name
        assert decoration.data.value.asText() == 'XXX'

    }

    @Test
    void 'Branch with filtered decorations and no match'() {
        def branch = doCreateBranch()
        setProperty branch, TestDecoratorPropertyType, new TestDecorationData("XXX", true)

        def data = run("""{
                branches (id: ${branch.id}) {
                    decorations(type: "unknown.Decorator") {
                        decorationType
                        data
                        error
                    }
                }   
            }""")

        def decorations = data.branches.first().decorations
        assert decorations != null
        assert decorations.empty
    }

}