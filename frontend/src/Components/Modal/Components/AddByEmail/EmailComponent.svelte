<script>
  import { createEventDispatcher } from "svelte";
  import ToggleComponent from "../ToggleComponent.svelte";
  import trashIcon from "../../../../assets/svg/trash-icon.svg";
  import { _ } from "svelte-i18n";

  export let email;
  export let role;
  export let pending = true;
  // Default is false, set to true if its the last email entry
  // used to check if it is the last element, if so bottom border
  // should be removed
  export let last = false;
  export let isUpdateComponent = false;

  let firstActive = role === 1;
  let secondActive = !firstActive;

  const dispatch = createEventDispatcher();

  function handleRemove() {
    dispatch("remove", email);
  }
  function handleToggleChange(event) {
    role = event.detail.selection;
    dispatch("roleChange", { email, role });
  }
</script>

<div class="entry-wrapper {last ? 'last' : ''}">
  <div class="entry-wrapper__email-wrapper">
    <input class:pending placeholder={email} disabled />
    <div class="entry-wrapper__icon-wrapper">
      <img
        src={trashIcon}
        alt={$_("modalEmailComponents.altRemove")}
        on:click={handleRemove}
        on:keydown={handleRemove}
      />
    </div>
  </div>
  <div class="entry-wrapper__toggle-wrapper">
    <ToggleComponent
      on:toggle={handleToggleChange}
      firstName={$_("modalEmailComponents.viewer")}
      secondName={$_("modalEmailComponents.editor")}
      {firstActive}
      {secondActive}
      {pending}
      {isUpdateComponent}
    />
  </div>
</div>

<style lang="scss">
  .entry-wrapper {
    border-bottom: 1px solid black;
    padding: 10px 5px;

    &.last {
      border-bottom: none;
    }

    &__email-wrapper {
      display: flex;
      margin-bottom: 10px;
      width: 100%;
    }
    &__icon-wrapper {
      display: flex;
      justify-content: center;

      img {
        width: 30px;
      }

      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }
    &__toggle-wrapper {
      width: 60%;
      margin-left: 5px;
      min-width: 200px;
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
    width: 100%;
    margin-right: 10px;
    background-color: var(--white);
  }
</style>
