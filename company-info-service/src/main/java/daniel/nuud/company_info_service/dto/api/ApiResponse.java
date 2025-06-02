package daniel.nuud.company_info_service.dto.api;

import lombok.Data;

@Data
public class ApiResponse {
    private Ticket results;
    private String status;
}
