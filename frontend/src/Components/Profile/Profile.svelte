<script>
  import { onMount } from "svelte";
  import { addToast } from "../Modal/ToastNotification/toastStore.js";
  import DeleteAccount from "../Modal/Components/DeleteAccount.svelte";
  import ChangePassword from "../Modal/Components/ChangePassword.svelte";
  import ChangeUsername from "../Modal/Components/ChangeUsername.svelte";
  import Modal from "../Modal/Modal.svelte";
  import { _ } from "svelte-i18n";

  const backendUrl = __BACKEND_URL__;
  let fullname = "";
  let email = "";
  let imgLink = "";
  let isAdmin = false;
  let error = "";
  let isModalOpen = false;
  let isPasswordModalOpen = false;
  let isUsernameModalOpen = false;
  let usernameValue = "";
  let passwordValue = "";
  let tempPassword = "";

  function toggleDeleteAccountModal() {
    isModalOpen = !isModalOpen;
  }
  function togglePasswordModal() {
    if (passwordValue == "" || tempPassword == "") {
      addToast({
        message: $_("profileTab.errors.emptyPassword"),
        type: "error",
      });
      return;
    } else if (passwordValue !== tempPassword) {
      addToast({
        message: $_("profileTab.errors.matchPassword"),
        type: "error",
      });
      return;
    } else {
      isPasswordModalOpen = !isPasswordModalOpen;
    }
  }
  function toggleUsernameModal() {
    if (usernameValue == "") {
      addToast({
        message: $_("profileTab.errors.emptyUsername"),
        type: "error",
      });
      return;
    } else {
      isUsernameModalOpen = !isUsernameModalOpen;
    }
  }

  function handleUsernameChange() {
    fetch(backendUrl + "/api/username:update", {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
      body: usernameValue,
    }).then((response) => {
      if (response.status !== 200) {
        addToast({
          message: $_("profileTab.errors.errorChangeUsername"),
          type: "error",
        });
      } else {
        addToast({
          message: $_("profileTab.usernameChanged"),
          type: "success",
        });
        usernameValue = "";
        window.location.href = backendUrl + "/logout";
      }
    });
  }
  function handlePasswordChange() {
    fetch(backendUrl + "/api/password:update", {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
      body: passwordValue,
    }).then((response) => {
      if (response.status !== 200) {
        addToast({
          message: $_("profileTab.errors.errorChangePassword"),
          type: "error",
        });
        isPasswordModalOpen = !isPasswordModalOpen;
      } else {
        addToast({
          message: $_("profileTab.passwordChanged"),
          type: "success",
        });
        passwordValue = "";
        tempPassword = "";
        isPasswordModalOpen = !isPasswordModalOpen;
      }
    });
  }
  function confirmAccountDeletion() {
    fetch(backendUrl + "/api/user", {
      method: "DELETE",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    }).then((response) => {
      if (response.status !== 200) {
        addToast({
          message: $_("profileTab.errors.errorDeleteAccount"),
          type: "error",
        });
      } else {
        addToast({
          message: $_("profileTab.accountDeleted"),
          type: "success",
        });
        window.location.href = backendUrl + "/logout";
      }
    });

    window.location.href = backendUrl + "/logout";
  }
  onMount(async () => {
    try {
      const response = await fetch(backendUrl + "/api/profile", {
        method: "GET",
        headers: {
          Application: "application/json",
        },
        credentials: "include",
      });
      if (response.status !== 200) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      fullname = data.name;
      email = data.email;
      imgLink = data.imageLink;
      isAdmin = data.isAdmin === "true";
    } catch (e) {
      error = e.message;
    }
  });
</script>

<div id="profile-container">
  <p class="admin">
    {#if isAdmin}
      {$_("profileTab.admin")}
    {/if}
  </p>
  <div id="preferences-container">
    <h1>{$_("profileTab.profile")}</h1>
    <div class="line-div"></div>
    <div class="config-container">
      <div class="introduction">
        <p class="title">{$_("profileTab.passwordContainer.changePassword")}</p>
        <p class="description">
          {$_("profileTab.passwordContainer.passwordDescription")}
        </p>
      </div>
      <div class="change-container">
        <input
          class="input-container"
          type="password"
          placeholder={$_("profileTab.passwordContainer.newPassword")}
          bind:value={tempPassword}
        />
        <input
          class="input-container"
          type="password"
          placeholder={$_("profileTab.passwordContainer.confirmPassword")}
          bind:value={passwordValue}
        />
        <button class="button--blue" on:click={togglePasswordModal}
          >{$_("profileTab.passwordContainer.confirmButton")}</button
        >
      </div>
    </div>
    {#if !imgLink}
      <div class="line-div"></div>
      <div class="config-container">
        <div class="introduction">
          <p class="title">
            {$_("profileTab.usernameContainer.changeUsername")}
          </p>
          <p class="description">
            {$_("profileTab.usernameContainer.usernameDescription")}
            <strong>{fullname}</strong><br /><br />
          </p>
        </div>
        <div class="change-container">
          <input
            class="input-container"
            type="text"
            placeholder={$_("profileTab.usernameContainer.newUsername")}
            bind:value={usernameValue}
          />
          <button class="button--blue" on:click={toggleUsernameModal}
            >{$_("profileTab.usernameContainer.confirmButton")}</button
          >
        </div>
      </div>
    {/if}
    <div class="line-div"></div>
    <div class="config-container">
      <div class="introduction">
        <p class="title">{$_("profileTab.accountContainer.deleteAccount")}</p>
        <p class="description">
          {$_("profileTab.accountContainer.deleteDescription")}
        </p>
      </div>
      <div class="change-container">
        <button class="button--red" on:click={toggleDeleteAccountModal}
          >{$_("profileTab.accountContainer.deleteButton")}</button
        >
      </div>
    </div>
  </div>
</div>
{#if isModalOpen}<Modal
    panelName={$_("profileTab.accountContainer.deleteAccount")}
    width="500px"
    on:closeModal={toggleDeleteAccountModal}
  >
    <DeleteAccount
      userEmail={email}
      on:confirm={confirmAccountDeletion}
      on:cancel={toggleDeleteAccountModal}
    /></Modal
  >
{/if}
{#if isPasswordModalOpen}<Modal
    panelName={$_("profileTab.passwordContainer.changePassword")}
    width="500px"
    on:closeModal={togglePasswordModal}
  >
    <ChangePassword
      userEmail={email}
      on:confirm={handlePasswordChange}
      on:cancel={togglePasswordModal}
    /></Modal
  >
{/if}
{#if isUsernameModalOpen}<Modal
    panelName={$_("profileTab.usernameContainer.changeUsername")}
    width="500px"
    on:closeModal={toggleUsernameModal}
  >
    <ChangeUsername
      oldUserName={fullname}
      newUserName={usernameValue}
      on:confirm={handleUsernameChange}
      on:cancel={toggleUsernameModal}
    /></Modal
  >
{/if}

<style>
  #profile-container {
    display: block;
    top: 0;
    width: 100%;
    height: 100%;
    margin-bottom: 50px;
    overflow: hidden;
  }
  #preferences-container {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    margin-top: 50px;
    align-items: center;
    height: 100%;
    width: 100%;
    position: relative;
  }
  .config-container {
    display: flex;
    border-radius: 15px;
    border: 1px solid var(--black);
    padding: 30px;
    margin-top: 25px;
    margin-bottom: 25px;
    width: 900px;
    height: 200px;
  }
  .change-container {
    display: flex;
    flex-direction: column;
    padding: 20px;
    align-items: flex-end;
    justify-content: flex-end;
    width: 40%;
    height: 500;
  }
  .input-container {
    margin-bottom: 20px;
    height: 30px;
    width: 100%;
  }
  .admin {
    font-size: 20px;
    font-weight: bold;
    margin-left: 30px;
    font-family: "Montserrat", sans-serif;
  }
  .line-div {
    min-width: 200px;
    justify-content: flex-start;
    max-width: 800px;
    width: 100%;
    height: 1px;
    background-color: var(--black);
  }
  .introduction {
    display: flex;
    flex-direction: column;
    width: 60%;
    height: 100%;
  }
  .title {
    display: flex;
    font-weight: bold;
    font-size: 25px;
    font-family: "Montserrat", sans-serif;
  }
  .description {
    height: 100%;
    font-weight: thin;
    font-family: "Montserrat", sans-serif;
  }
  .button--blue {
    width: 70%;
  }
  .button--red {
    border-radius: 5px;
    width: 70%;
    height: 30px;
    border: #000 1px solid;
    font-family: "Rubik", sans-serif;
    background-color: var(--bittersweet);
    box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);
    font-size: 16px;
  }
  .button--red:hover {
    cursor: pointer;
    background-color: var(--mandy);
  }
  @media (max-width: 1100px) {
    .config-container {
      flex-wrap: wrap;
      height: 30%;
      width: 60%;
    }
    .input-container {
      width: 100%;
    }
    .introduction {
      width: 100%;
    }
    .change-container {
      width: 100%;
    }
  }
</style>
