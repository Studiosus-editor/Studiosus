import { addToast } from "../../Modal/ToastNotification/toastStore.js";

const backendUrl = __BACKEND_URL__;

export async function fetchProjectStructure(projectId, message) {
    return fetch(backendUrl + `/api/project/${projectId}/folders`, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }

export async function fetchTemplateStructure(projectId, message) {
  return fetch(backendUrl + `/api/template/${projectId}/folders`, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
    credentials: "include",
  })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
}

export async function createFolder(projectId, message, folderName, selectedFolderId) {
    return fetch(
      backendUrl + `/api/project/${projectId}/folder/${selectedFolderId}`,
      {
        method: "POST",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
        body: folderName,
      }
    )
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        return { id: data.id };
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }

export async function createFile(projectId, message, filename, selectedFolderId, content = null) {
    const fetchOptions = {
      method: "POST",
      headers: {
        Accept: "application/json"
      },
      credentials: "include",
    };

    if (content !== null) {
      fetchOptions.body = content;
    }

    return fetch(
      `${backendUrl}/api/project/${projectId}/folder/${selectedFolderId}/file/${filename}`,
      fetchOptions
    )
    .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        return { id: data.id, name: data.name };
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
}

export async function fetchFileData(projectId, message, currentFile, isTemplate = false) {
    const templateOrFolder = isTemplate ? "template" : "project";

    return fetch(
      backendUrl + `/api/${templateOrFolder}/${projectId}/file/${currentFile}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
      }
    )
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }

export async function handleDeleteFile(projectId, message, fileId) {
    return fetch(
      backendUrl + `/api/project/${projectId}/file/${fileId}`,
      {
        method: "DELETE",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
      }
    )
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }

export async function deleteFolder(projectId, message, folderId) {
    return fetch(backendUrl + `/api/project/${projectId}/folder/${folderId}`, {
      method: "DELETE",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }
export  async function renameFile(projectId, message, id, newName) {
    return fetch(backendUrl + `/api/project/${projectId}/file/${id}:rename`, {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
      body: newName,
    })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }
export async function moveFile(projectId, message, itemId, moveToFolderId) {
    return fetch(
        backendUrl +
        `/api/project/${projectId}/file/${itemId}/${moveToFolderId}`,
        {
        method: "PUT",
        headers: {
            Accept: "application/json",
        },
        credentials: "include",
        }
    )
    .then((response) => {
        if (!response.ok) {
            addToast({
                message: message,
                type: "error",
            });
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
    })
    .then((data) => {
        return data;
    })
    .catch(() => {
        addToast({
            message: message,
            type: "error",
        });
        return null;
    });
}
export async function moveFolder(projectId, message, itemId, moveToFolderId) {
    return fetch(
      backendUrl +
        `/api/project/${projectId}/folder/${itemId}/${moveToFolderId}`,
      {
        method: "PUT",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
      }
    )
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }
export async function renameFolder(projectId, message, id, newName) {
    return fetch(backendUrl + `/api/project/${projectId}/folder/${id}:rename`, {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
      body: newName,
    })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  }