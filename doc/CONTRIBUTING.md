# Rules for contributing

- This project is written using `Java` version 21, therefore new commits shall not use features from newer Java version.
- For this project, we utilize the `Gradle` build automation system. It's **essential** that all third-party tools, where feasible, are declared and installed via the `build.gradle` file. This ensures a streamlined and consistent setup process.
- The project follows [Google's Java style guide](https://google.github.io/styleguide/javaguide.html). Proper format of the java code is automatically checked for each commit in `CI/CD` pipeline.
- Our `CI/CD` pipeline consists of 3 steps: _Build_, _Format_, _Test_.
  _Build_ - checks if source code compiles succesfully
  _Format_ - checks if source code is formated properly
  _Test_ - checks if source code passes all automatic tests
- Merge request must pass all stages of `CI/CD` pipeline before being merged.
- Merge request must be approved by atleast 1 team member before being merged.
- Each new created issue must have at least one of the 4 labels assigned to it: feature, task, bug, research.
  feature - related to addition of new funcionality or modification of already written code.
  task - related to modification of documentation or any other task that does not involve code modification.
  bug - related to solving a bug that has occured inside the written code.
  research - related to researching an implementation of a new idea that would benefit our program.
- Each new created branch should follow this structure: label/issue_number-task-name-goes-here.
  example: task/134-update-readme-structure
- Each made commit to a branch should have its issue number attached at the end.
  example: - update readme structure(#134)