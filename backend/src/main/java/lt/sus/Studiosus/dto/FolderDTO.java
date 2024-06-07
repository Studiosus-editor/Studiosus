package lt.sus.Studiosus.dto;

import java.util.List;

public record FolderDTO(
    Long id, String name, Long parentFolderId, List<FileDTO> files, List<FolderDTO> childFolders) {}
