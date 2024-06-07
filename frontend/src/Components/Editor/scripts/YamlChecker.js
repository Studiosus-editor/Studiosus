import jsyaml from "js-yaml";
import { errorStore } from './store.js';

export function validateYAML(yamlCode) {
    let lastErrorMessage = null;
    let _errorLine = null;
    let _errorColumn = null;

    // removes HTML no break space characters
    yamlCode = yamlCode.replace(/\u00A0/g, " ");

    try {
        jsyaml.load(yamlCode);
        if (!yamlCode.trim()) {
            errorStore.set("Empty YAML file!");
            return;
        }
    } catch (error) {
        let errorMessage = error.message; // Get the whole error message
        let errorLocationMatch = errorMessage.match(/\((\d+):(\d+)\)/);
        if (errorLocationMatch) {
            _errorLine = parseInt(errorLocationMatch[1]);
            _errorColumn = parseInt(errorLocationMatch[2]);
        }
        lastErrorMessage = errorMessage;
        errorStore.set(lastErrorMessage);
        return;
    }
    errorStore.set("Correct YAML syntax!");
    return;
}
