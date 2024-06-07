<script>
    import { createEventDispatcher } from 'svelte';
    import { _ } from "svelte-i18n";
    export let oldUserName;
    export let newUserName;
    const dispatch = createEventDispatcher();
  
    function confirm() {
      dispatch('confirm');
    }
  
    function cancel() {
      dispatch('cancel');
    }
  </script>
  
  <div class="modal">
    <h3>{$_("profileTab.usernameContainer.confirmUsernameModal.usernameQuestion")}<br><br>
    {$_("profileTab.usernameContainer.confirmUsernameModal.currentUsername")} {oldUserName}<br>
    {$_("profileTab.usernameContainer.confirmUsernameModal.newUsername")} <strong>{newUserName}</strong>
    </h3>
    <button class= "button--blue" on:click={cancel}>{$_("profileTab.usernameContainer.confirmUsernameModal.cancelButton")}</button>
    <button class="button--blue" on:click={confirm}>{$_("profileTab.usernameContainer.confirmUsernameModal.changeButton")}</button>
  </div>
  
  <style>
    .button--blue {
      width:45%;
    }
  </style>