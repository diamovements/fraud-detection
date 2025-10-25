package hackathon.project.fraud_detection.rules.ml;

import hackathon.project.fraud_detection.rules.model.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MLModelService {

    private final Path directory;

    public MLModelService(@Value("${ml.models.directory}") String modelsDir) {
        this.directory = Paths.get(modelsDir).toAbsolutePath();
        createModelsDirectory();
    }

    private void createModelsDirectory() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelInfo saveModel(MultipartFile file, String modelName) {
        Path targetPath = directory.resolve(modelName);
        if (Files.exists(targetPath)) {
            throw new IllegalArgumentException("Model with name '" + modelName + "' exists");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return new ModelInfo(modelName, Files.size(targetPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path filePath : stream) {
                log.info("Loading file {}", filePath);
                if (Files.isRegularFile(filePath) && isModelFile(filePath)) {
                    models.add(new ModelInfo(filePath.getFileName().toString(), Files.size(filePath)));
                }
            }
        } catch (IOException e) {
            log.error("Error while reading models directory: {}", e.getMessage());
        }

        return models;
    }

    private boolean isModelFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".pkl");
    }

}
