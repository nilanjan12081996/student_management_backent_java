package skyware.product.skywareapi.sidebar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skyware.product.skywareapi.sidebar.dto.SidebarRequestDto;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;
import skyware.product.skywareapi.sidebar.service.SidebarService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("skyware/api/sidebar")
public class SidebarController {

    private final SidebarService sidebarService;
    @PostMapping("/add")
    public ResponseEntity<?> sideBarAdd(@Valid @RequestBody SidebarRequestDto sidebarRequestDto, BindingResult bindingResult){
        Map<String,Object> response = new HashMap<>();
        if(bindingResult.hasErrors()){
            Map<String,String> errors = new HashMap<>();
            for(FieldError error : bindingResult.getFieldErrors()){
                errors.put(error.getField(),error.getDefaultMessage());
            }
            response.put("message","validation error");
            response.put("status",false);
            response.put("statusCode",422);
            response.put("error",errors);

            return ResponseEntity.status(422).body(response);
        }
        try{
            sidebarService.saveSidebar(sidebarRequestDto);
            response.put("message","Sidebar added successfully");
            response.put("status",true);
            response.put("statusCode",201);
            return ResponseEntity.status(201).body(response);
        }catch(Exception e){
            response.put("message",e.getMessage());
            response.put("status",false);
            response.put("statusCode",400);
            response.put("error",e.getStackTrace());

            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> sideBarList(){
        Map<String,Object> response = new HashMap<>();
        try {
            response.put("message","Sidebar listed successfully");
            response.put("status",true);
            response.put("statusCode",200);
            response.put("data", sidebarService.getAllSidebars());
            return ResponseEntity.status(200).body(response);
        } catch(Exception e) {
            response.put("message",e.getMessage());
            response.put("status",false);
            response.put("statusCode",400);
            return ResponseEntity.status(400).body(response);
        }
    }
}
