<script>
  import { clearableInput } from "../../../utils/clearableInput.js";
  export let projectName = "";
  import { _ } from "svelte-i18n";
</script>

<div class="content-wrapper">
  <div class="content-wrapper__name-wrapper">
    <h3>
      <label for="pname">{$_("modalNameProject.projectName")}</label>
    </h3>
    <input
      type="text"
      id="pname"
      name="pname"
      placeholder={$_("modalNameProject.placeholderMyProject")}
      bind:value={projectName}
      use:clearableInput
      onclick="this.placeholder = '';"
      onblur="if(this.value=='') this.placeholder='{$_(
        'modalNameProject.placeholderMyProject'
      )}';"
    />
  </div>
</div>

<style lang="scss">
  .content-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 110px 0;

    &__name-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
    }
  }
  input {
    height: 40px;
    text-align: center;
    font-family: "Rubik", sans-serif;
    font-weight: 400;
    font-size: 24px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
    border: 1px solid;
  }

  h3 {
    margin-top: 0;
    font-weight: 500;
  }
</style>
