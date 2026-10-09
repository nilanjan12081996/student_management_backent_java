package skyware.product.skywareapi.mapsidebar.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skyware.product.skywareapi.mapsidebar.dto.RoleSidebarMapDto;
import skyware.product.skywareapi.mapsidebar.service.RoleSidebarService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("skyware/api/map")
public class RoleSidebarMapController {
    public final RoleSidebarService roleSidebarService;
    @PostMapping("/map-sidebar")
    public ResponseEntity<?> addSidebarMap(@Valid @RequestBody RoleSidebarMapDto roleSidebarMapDto , BindingResult bindingResult){
        Map<String,Object> response = new HashMap<>();
        try{

            RoleSidebarMapDto role = roleSidebarService.roleMap(roleSidebarMapDto);
            response.put("message","data save");
            response.put("statusCode",200);
            response.put("data",role);
            response.put("status",true);

            return ResponseEntity.status(200).body(response);
        }catch(Exception e){
            response.put("message","");
            response.put("statusCode",400);
            response.put("status",false);

            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMappedSidebars(){
        Map<String,Object> response = new HashMap<>();
        try{
            response.put("message","Data fetched successfully");
            response.put("statusCode",200);
            response.put("data",roleSidebarService.getAllMappedSidebars());
            response.put("status",true);

            return ResponseEntity.status(200).body(response);
        }catch(Exception e){
            response.put("message",e.getMessage());
            response.put("statusCode",400);
            response.put("status",false);

            return ResponseEntity.status(400).body(response);
        }
    }
}
