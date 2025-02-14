package pl.kamann.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamann.dtos.FacilityDto;
import pl.kamann.services.admin.FacilityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facility")
public class FacilityController {
    private final FacilityService facilityService;

    @GetMapping("/{id}")
    @Operation(summary = "Get Facility", description = "Get Facility")
    public ResponseEntity<FacilityDto> getFacility(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.getFacility(id));
    }

    @PostMapping
    @Operation(summary = "Create Facility", description = "Create Facility")
    public ResponseEntity<FacilityDto> createFacility(@RequestBody FacilityDto facilityDto) {
        return ResponseEntity.ok(facilityService.createFacility(facilityDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Facility", description = "Update Facility")
    public ResponseEntity<FacilityDto> updateFacility(@PathVariable Long id, @RequestBody FacilityDto facilityDto) {
        return ResponseEntity.ok(facilityService.updateFacility(id, facilityDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Facility", description = "Delete Facility")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
