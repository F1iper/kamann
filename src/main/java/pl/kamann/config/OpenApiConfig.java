package pl.kamann.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Local server");

        Info info = new Info()
                .title("Dance dance")
                .version("1.0")
                .description("A modern reservation system built with Java 21 and Spring Boot 3, featuring role-based access control, membership management, and class scheduling.A modern reservation system built with Java 21 and Spring Boot 3, featuring role-based access control, membership management, and class scheduling.");

        Components components = new Components().addSecuritySchemes("bearer-jwt",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")
        );

        List<Tag> tags = List.of(
                new Tag().name("1. login").description("Auth Controller"),
                new Tag().name("2. client event controller").description("Fetch events and occurrences with filtering and pagination."),
                new Tag().name("3. admin event controller").description("Control events and event occurences from admin perspective.")
        );

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addServersItem(devServer)
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .tags(tags);


//        // AdminAttendanceController
//        openAPI.path("/api/admin/attendance/{eventId}/{clientId}/cancel", new PathItem().post(
//                new Operation()
//                        .summary("Cancel client attendance")
//                        .description("Cancels the attendance of a client for a specific event.")));
//
//        openAPI.path("/api/admin/attendance/{eventId}/{clientId}/mark", new PathItem().post(
//                new Operation()
//                        .summary("Mark attendance")
//                        .description("Marks the attendance status for a specific client in a specific event.")));
//
//
//        // AdminEventController
//        openAPI.path("/api/v1/admin/events", new PathItem().get(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("List events")
//                        .description("Admins can list all events, or filter by instructor if an instructor ID is provided.")));
//
//        openAPI.path("/api/v1/admin/events", new PathItem().post(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Create an event")
//                        .description("Creates a new event and assigns an instructor.")));
//
//        openAPI.path("/api/v1/admin/events/{id}", new PathItem().get(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Get event details")
//                        .description("Retrieves detailed information about a specific event.")));
//
//        openAPI.path("/api/v1/admin/events/{id}", new PathItem().patch(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Update event details")
//                        .description("Updates event details partially – only fields provided in the request are updated.")));
//
//        openAPI.path("/api/v1/admin/events/{id}/cancel", new PathItem().post(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Cancel event")
//                        .description("Cancels an event and notifies all participants.")));
//
//        openAPI.path("/api/v1/admin/events/{id}", new PathItem().delete(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Delete event")
//                        .description("Deletes an event by its ID.")));
//
//        openAPI.path("/api/v1/admin/events/{id}/force", new PathItem().delete(
//                new Operation()
//                        .tags(List.of("3. admin event controller"))
//                        .summary("Force delete event")
//                        .description("Deletes an event even if participants are registered.")));
//
//
//        // AdminMembershipCardController
//        openAPI.path("/api/admin/membership-cards/approve/{cardId}", new PathItem().post(
//                new Operation()
//                        .summary("Approve client card request")
//                        .description("Approves a client's membership card request.")));
//
//        openAPI.path("/api/admin/membership-cards/create", new PathItem().post(
//                new Operation()
//                        .summary("Create membership card")
//                        .description("Creates a new predefined membership card.")));
//
//        openAPI.path("/api/admin/membership-cards/renew/{userId}", new PathItem().post(
//                new Operation()
//                        .summary("Renew membership card")
//                        .description("Renews a user's membership card.")));
//
//
//        // AdminUserController
//        openAPI.path("/api/admin/users", new PathItem().get(
//                new Operation()
//                        .summary("Get all users with pagination")
//                        .description("Retrieve a paginated list of all users in the system filtered by role.")));
//
//        openAPI.path("/api/admin/users", new PathItem().post(
//                new Operation()
//                        .summary("Register a new user")
//                        .description("Registers a new user (client, instructor, or admin) with specified details and roles. The roles and other information are provided in the request body.")));
//
//        openAPI.path("/api/admin/users/activate/{userId}", new PathItem().put(
//                new Operation()
//                        .summary("Activate user account")
//                        .description("Activates a user account by setting its status to ACTIVE. This endpoint requires the user's ID.")));
//
//        openAPI.path("/api/admin/users/deactivate/{userId}", new PathItem().put(
//                new Operation()
//                        .summary("Deactivate user account")
//                        .description("Deactivates a user account by setting its status to INACTIVE. This endpoint requires the user's ID.")));
//
//        openAPI.path("/api/admin/users/logged", new PathItem().get(
//                new Operation()
//                        .summary("Get details of logged in user")
//                        .description("Change the status of a user to ACTIVE, INACTIVE, or any other supported status. The new status is provided as a query parameter.")));
//
//
//        // ClientAttendanceController
//        openAPI.path("/api/client/attendance/{eventId}/cancel", new PathItem().post(
//                new Operation()
//                        .summary("Cancel attendance")
//                        .description("Cancels the client's attendance for the specified event.")));
//
//        openAPI.path("/api/client/attendance/{eventId}/join", new PathItem().post(
//                new Operation()
//                        .summary("Join an event")
//                        .description("Registers the logged-in client to the specified event.")));
//
//        openAPI.path("/api/client/attendance/summary", new PathItem().get(
//                new Operation()
//                        .summary("Get attendance summary")
//                        .description("Retrieves the attendance summary for the logged-in client.")));
//
//
//        // ClientEventController
//        openAPI.path("/api/client/events", new PathItem().get(
//                new Operation()
//                        .summary("Get paginated events")
//                        .description("LRetrieves a paginated list of events.")));
//
//        openAPI.path("/api/client/events/{id}", new PathItem().get(
//                new Operation()
//                        .summary("Get event details by ID")
//                        .description("Retrieve details of a specific Event using its unique ID.")));
//
//
//        // ClientMembershipCardController
//        openAPI.path("/api/client/membership-cards", new PathItem().get(
//                new Operation()
//                        .summary("Get client membership card")
//                        .description("Retrieves the client's membership card details.")));
//
//        openAPI.path("/api/client/membership-cards/request", new PathItem().post(
//                new Operation()
//                        .summary("Request membership card")
//                        .description("Requests a new membership card for the client.")));
//
//        openAPI.path("/api/client/membership-cards/renew", new PathItem().post(
//                new Operation()
//                        .summary("Renew membership card")
//                        .description("Renews the client's membership card.")));
//
//
//        // ClientOccurrenceController
//        openAPI.path("/api/client/occurrences", new PathItem().get(
//                new Operation()
//                        .summary("List occurrences")
//                        .description("Lists all occurrences available for clients to join.")));
//
//        openAPI.path("/api/client/occurrences/{id}", new PathItem().get(
//                new Operation()
//                        .summary("Get occurrence details")
//                        .description("Retrieves detailed information about a specific occurrence.")));

        return openAPI;
    }
}
