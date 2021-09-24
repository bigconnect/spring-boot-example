package io.bigconnect.springbootexample.rest;

import com.mware.core.model.clientapi.dto.ClientApiSchema;
import com.mware.core.model.clientapi.dto.ClientApiUser;
import com.mware.core.model.clientapi.dto.ClientApiVertex;
import com.mware.core.model.clientapi.dto.ClientApiWorkspace;
import com.mware.core.model.schema.SchemaRepository;
import com.mware.core.model.user.UserRepository;
import com.mware.core.model.workspace.WorkspaceRepository;
import com.mware.core.user.SystemUser;
import com.mware.core.util.ClientApiConverter;
import com.mware.core.util.StreamUtil;
import com.mware.ge.Authorizations;
import com.mware.ge.FetchHints;
import com.mware.ge.Graph;
import com.mware.ge.Vertex;
import com.mware.ge.collection.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/example")
public class DemoRestController {
    private final Logger LOG = LoggerFactory.getLogger(DemoRestController.class);

    final Graph graph;
    final SchemaRepository schemaRepository;
    final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public DemoRestController(Graph graph, SchemaRepository schemaRepository, WorkspaceRepository workspaceRepository, UserRepository userRepository) {
        this.graph = graph;
        this.schemaRepository = schemaRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/concepts")
    public ResponseEntity<List<ClientApiSchema.Concept>> getConcepts() {
        ClientApiSchema clientApiSchema = schemaRepository.getClientApiObject(SchemaRepository.PUBLIC);
        return ResponseEntity.ok(clientApiSchema.getConcepts());
    }

    @GetMapping("/workspaces")
    public ResponseEntity<List<ClientApiWorkspace>> getWorkspaces() {
        List<ClientApiWorkspace> workspaces = StreamUtil.stream(workspaceRepository.findAll(new SystemUser()))
                .map(w -> workspaceRepository.toClientApi(w, new SystemUser(), null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ClientApiUser>> getUsers() {
        List<ClientApiUser> users = StreamUtil.stream(userRepository.find(null))
                .map(userRepository::toClientApi)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/vertices")
    public ResponseEntity<List<ClientApiVertex>> getVertices() {
        Iterable<Vertex> vertices = graph.getVertices(FetchHints.ALL, new Authorizations());
        List<ClientApiVertex> result = new ArrayList<>();
        for (Vertex v : Iterables.limit(10, vertices)) {
            result.add(ClientApiConverter.toClientApiVertex(v, null, new Authorizations()));
        }
        return ResponseEntity.ok(result);
    }
}
