<script>
  import { createEventDispatcher, onMount } from "svelte";
  import { _ } from "svelte-i18n";
  export let userEmail;
  let inputClass = "deletion-confirmation";
  let tempEmail = "";
  let placeholderText = $_(
    "profileTab.accountContainer.deleteAccountModal.emailPlaceholder"
  ); // default placeholder
  const dispatch = createEventDispatcher();

  function confirm() {
    if (checkEmail()) {
      dispatch("confirm");
    }
  }

  function cancel() {
    dispatch("cancel");
  }

  function checkEmail() {
    if (tempEmail === userEmail) {
      return true;
    } else {
      tempEmail = "";
      inputClass = "deletion-confirmation error";
      placeholderText = $_(
        "profileTab.accountContainer.deleteAccountModal.emailNotMatch"
      );
      return false;
    }
  }

  function handleInput(event) {
    tempEmail = event.target.value;
    if (inputClass.includes("error")) {
      inputClass = "deletion-confirmation";
      placeholderText = $_(
        "profileTab.accountContainer.deleteAccountModal.emailPlaceholder"
      );
    }
  }
</script>

<div class="modal">
  <h3>
    {$_("profileTab.accountContainer.deleteAccountModal.accountQuestion")}
    {$_("profileTab.accountContainer.deleteAccountModal.accountQuestion2")}
    {userEmail}
    {$_("profileTab.accountContainer.deleteAccountModal.accountQuestion3")}
    <br />
    {$_("profileTab.accountContainer.deleteAccountModal.inputEmail")}
  </h3>
  <input
    class={inputClass}
    placeholder={placeholderText}
    bind:value={tempEmail}
    on:input={handleInput}
  />
  <button class="button--blue" on:click={cancel}
    >{$_("profileTab.accountContainer.deleteAccountModal.cancelButton")}</button
  >
  <button class="button-red" on:click={confirm}
    >{$_("profileTab.accountContainer.deleteAccountModal.deleteButton")}</button
  >
</div>

<style>
  .button--blue {
    width: 45%;
  }
  .button-red {
    border-radius: 5px;
    width: 45%;
    height: 30px;
    border: #000 1px solid;
    font-family: "Rubik", sans-serif;
    background-color: var(--bittersweet);
    box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);
    font-size: 16px;
  }
  .button-red:hover {
    cursor: pointer;
    background-color: var(--mandy);
  }
  .deletion-confirmation {
    margin-bottom: 20px;
    height: 30px;
    width: 100%;
  }
  .deletion-confirmation.error::placeholder {
    color: red;
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
</style>
