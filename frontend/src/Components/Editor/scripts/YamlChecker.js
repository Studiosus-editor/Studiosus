import jsyaml from "js-yaml";

export default class YamlChecker {
  constructor(yamlCode) {
    this.yamlCode = yamlCode;
    this.lastErrorMessage = null;
    this.lastErrorYamlCode = null;
    this._errorLine = null;
    this._errorColumn = null;
  }

  get errorLine() {
    return this._errorLine;
  }

  get errorColumn() {
    return this._errorColumn;
  }
  //whole error message

  validateYAML() {
    try {
      jsyaml.load(this.yamlCode);
      if (!this.yamlCode.trim()) {
              return "Empty YAML file!";
            }
      this.lastErrorMessage = null;
      this._errorLine = null;
      this._errorColumn = null;
    } catch (error) {
      let errorMessage = error.message; // Get the whole error message
      let errorLocationMatch = errorMessage.match(/\((\d+):(\d+)\)/);
      if (errorLocationMatch) {
        this._errorLine = parseInt(errorLocationMatch[1]);
        this._errorColumn = parseInt(errorLocationMatch[2]);
      }
      this.lastErrorMessage = errorMessage;
      return this.lastErrorMessage;
    }
     return "Correct YAML syntax!"
  }
  formatYAML() {
    const formattedYaml = jsyaml.dump(jsyaml.load(this.yamlCode));
    return formattedYaml;
  }

  //Don't delete this, possible future feature

  // async fetchGeminiData(yamlCode) {
  //   const model = genAI.getGenerativeModel({ model: "gemini-pro" });
  //   const prompt = "Fix the following YAML syntax error in this yaml code without any other comments: \n" + yamlCode;
    
  //   const result = await model.generateContent(prompt);
  //   console.log(result.response.text());
  // }
  
  // //only first line of error message
  // validateYAML() {
  //   try {
  //     jsyaml.load(this.yamlCode);
  //     if (!this.yamlCode.trim()) {
  //       return "Empty YAML file!";
  //     }
  //     this.lastErrorMessage = null;
  //     this._errorLine = null;
  //     this._errorColumn = null;
  //   } catch (error) {
  //     let errorMessage = error.message.split('\n')[0]; // Get only the first line of the error message
  //     let errorLocationMatch = errorMessage.match(/\((\d+):(\d+)\)/);
  //     if (errorLocationMatch) {
  //       this._errorLine = parseInt(errorLocationMatch[1]);
  //       this._errorColumn = parseInt(errorLocationMatch[2]);
  //     }
  //     this.lastErrorMessage = errorMessage;
  //     return this.lastErrorMessage;
  //   }
  //   return "Correct YAML syntax!"
  // }

}