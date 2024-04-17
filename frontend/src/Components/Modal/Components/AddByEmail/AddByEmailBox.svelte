<script>
  import ToggleComponent from "../ToggleComponent.svelte";
  import { clearableInput } from "../../../../utils/clearableInput.js";
  import EmailComponent from "./EmailComponent.svelte";
  import { _ } from "svelte-i18n";

  export let showHeader = true;
  export let emailEntries;

  const placeholderEmail = $_("modalEmailComponents.placeholderEmail");
  const emailCannotBeEmpty = $_("modalEmailComponents.emailCannotBeEmpty");
  const invalidEmail = $_("modalEmailComponents.invalidEmail");
  const emailAlreadyAdded = $_("modalEmailComponents.emailAlreadyAdded");

  const EMAIL_VALIDATION_REGEX = /^\b[\w.-]+@[\w-]+\.[A-Za-z]{2,}\b$/;
  let email = "";
  let role = 1;
  let placeholder = placeholderEmail;
  let inputClass;

  // on click clear any error class from input
  function handleInputClick() {
    inputClass = "";
    placeholder = "";
  }

  // on blur if email is empty set placeholder to "Email"
  // unless the next focus target is the submit button if so
  // do nothing to prevent default placeholder from breefly showing
  function handleBlur(event) {
    const nextFocusTarget = event.relatedTarget;
    if (nextFocusTarget && nextFocusTarget.id !== "submit-button") {
      if (email === "") {
        placeholder = placeholderEmail;
      }
    }
  }

  // get the role from the toggle component
  function handleToggleChange(event) {
    role = event.detail.selection;
  }

  // handle role change if it happens inside EmailComponent
  function handleRoleChange(emailToUpdate, newRole) {
    const entryIndex = emailEntries.findIndex(
      (entry) => entry.email === emailToUpdate
    );
    if (entryIndex !== -1) {
      emailEntries[entryIndex].role = newRole;
    }
  }

  function removeEmail(emailToRemove) {
    emailEntries = emailEntries.filter(
      (entry) => entry.email !== emailToRemove
    );
  }

  // if email is empty or doesnt contain @ symbol change placeholder to error message,
  // else pushes new email to email array if it doesnt already exist
  function handleSubmit() {
    if (!email) {
      inputClass = "error";
      placeholder = emailCannotBeEmpty;
      return;
    } else if (!email.match(EMAIL_VALIDATION_REGEX)) {
      email = "";
      inputClass = "error";
      placeholder = invalidEmail;
      return;
    }
    // Check if the email already exists in the array
    const emailExists = emailEntries.some((entry) => entry.email === email);
    if (emailExists) {
      email = "";
      inputClass = "error";
      placeholder = emailAlreadyAdded;
      return;
    }
    // Push a new entry into the emails array
    emailEntries = [...emailEntries, { email, role }];
  }
</script>

<div class="email-section">
  {#if showHeader}
    <h3><label for="email">{$_("modalEmailComponents.addByEmail")}</label></h3>
  {/if}
  <div class="email-section__email-input-wrapper">
    <input
      type="email"
      name="email"
      class={inputClass}
      {placeholder}
      bind:value={email}
      on:click={handleInputClick}
      on:blur={handleBlur}
      use:clearableInput
    />
    <button id="submit-button" class="button--blue" on:click={handleSubmit}
      >+</button
    >
  </div>
  <div class="email-section__main-toggle-wrapper">
    <div class="email-section__main-toggle-container">
      <ToggleComponent
        on:toggle={handleToggleChange}
        firstName={$_("modalEmailComponents.viewer")}
        secondName={$_("modalEmailComponents.editor")}
      />
    </div>
  </div>
  <div class="email-section__list-wrapper">
    {#if emailEntries.length === 0}
      <div class="email-section__empty-text-wrapper">
        <h3>{$_("modalEmailComponents.emptyEmailList")}</h3>
      </div>
    {/if}
    {#each emailEntries as { email, role }, index (email)}
      <EmailComponent
        {email}
        {role}
        last={index === emailEntries.length - 1}
        on:remove={() => removeEmail(email)}
        on:roleChange={({ detail: { role } }) => handleRoleChange(email, role)}
      />
    {/each}
  </div>
</div>

<style lang="scss">
  .email-section {
    margin-bottom: 24px;

    &__empty-text-wrapper {
      display: flex;
      align-items: center;
      height: 100%;
      justify-content: center;
      h3 {
        font-weight: 400;
      }
    }

    &__main-toggle-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 10px;
    }

    &__main-toggle-container {
      min-width: 300px;
    }

    &__list-wrapper {
      display: flex;
      flex-direction: column;
      height: 140px;
      max-height: 140px;
      overflow-y: auto;
      margin-top: 20px;
      background-color: var(--white-smoke);
      width: 96.5%;
      border-radius: 5px;
      border: solid 1px black;
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.15);
      overflow-x: hidden;

      &::-webkit-scrollbar {
        width: 5px;
      }
      &::-webkit-scrollbar-track {
        background: var(--grey85);
      }

      &::-webkit-scrollbar-thumb {
        background-color: var(--periwinkle);
        border-radius: 5px;
        border: 1px solid var(--black);
        border-right: none;
      }
    }

    &__email-input-wrapper {
      display: flex;
      width: 100%;
      margin-top: 24px;
      align-items: center;

      input {
        margin-right: 10px;
        width: 100%;

        &.error {
          border: 1px solid red;
          &::placeholder {
            color: red;
          }
        }
      }
    }

    button {
      width: 15%;
      font-family: 500;
      height: 40px;
      font-size: 24px;
    }
  }

  input {
    height: 40px;
    text-align: left;
    font-family: "Rubik", sans-serif;
    font-weight: 400;
    font-size: 24px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
    border: 1px solid;
    padding-left: 5px;
  }

  h3 {
    margin: 22px 0;
    font-weight: 500;
  }
</style>
