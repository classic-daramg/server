package com.daramg.server.common.exception;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.CommonErrorStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exception")
public class ExceptionTestController {

    @GetMapping("/business")
    public void throwBusinessException() {
        throw new BusinessException(CommonErrorStatus.FORBIDDEN);
    }

    @PostMapping("/validation")
    public void throwValidation(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/unexpected")
    public void throwUnexpectedException() {
        throw new RuntimeException();
    }

    public record TestRequest(
            @NotBlank(message = "COMMON_400") String field1,
            @Size(max = 3, message = "COMMON_400") String field2
    ){}
}
