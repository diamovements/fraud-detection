package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.rules.ml.MLModelService;
import hackathon.project.fraud_detection.rules.model.ModelInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MLController {

    private final MLModelService mlModelService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadModel(
            @RequestParam("model") MultipartFile file,
            @RequestParam("modelName") String modelName) {
        try {
            ModelInfo modelInfo = mlModelService.saveModel(file, modelName + ".pkl");
            return ResponseEntity.ok(modelInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/models")
    public ResponseEntity<List<ModelInfo>> getModels() {
        return ResponseEntity.ok(mlModelService.getAvailableModels());
    }
}
