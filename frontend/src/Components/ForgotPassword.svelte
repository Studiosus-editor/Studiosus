<script>
  import { _ } from "svelte-i18n";
  import { addToast } from "./Modal/ToastNotification/toastStore.js";

  const backendUrl = __BACKEND_URL__;

  let isLoading = false;

  async function handleSubmit(event) {
    event.preventDefault();
    const email = event.target.elements.email.value;

    if (email === "") {
      addToast({
        message: $_("forgotPassword.toastNotifications.emptyEmail"),
        type: "error",
      });
      return;
    }

    isLoading = true;

    const response = await fetch(backendUrl + "/api/forgot-password", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: email,
    });

    if (response.status === 200) {
      addToast({
        message: $_("forgotPassword.toastNotifications.success"),
        type: "success",
      });
    } else {
      addToast({
        message: $_("forgotPassword.toastNotifications.error"),
        type: "error",
      });
    }
    isLoading = false;
  }
</script>

<div class="main-component">
  <h1>Studiosus</h1>
  <div class="login-component">
    <div class="password-login">
      <form on:submit={handleSubmit}>
        <input
          type="email"
          id="email"
          name="email"
          placeholder={$_("login.email")}
        />
        <div class="button-container">
          {#if !isLoading}
            <button class="button--blue" type="submit"
              >{$_("forgotPassword.resetPassword")}</button
            >
          {:else}
            <div class="loader"></div>
          {/if}
        </div>
      </form>
      <a href="/login">{$_("forgotPassword.goBack")}</a>
    </div>
  </div>
</div>

<style>
  .button-container {
    margin-top: 24px;
    width: 50%;
    display: flex;
    justify-content: center;
  }

  .main-component {
    margin: 100px 0 100px 0;
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.5);
    text-align: center;
    width: 400px;
    transition: all 0.3s ease;
    position: relative;
    padding: 15px;
    border-radius: 5%;
    border: 2px solid #000;
    background-color: var(--grey85);
  }

  .password-login {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }
  form {
    border-top: 3px solid #000;
    padding-top: 10px;
    margin: 0px 0px 20px 0px;
    width: 90%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }

  input {
    width: 90%;
    padding: 10px 0px 10px 0px;
    margin: 5px 0 5px 0px;
    font-size: 1rem;
  }

  input::placeholder {
    padding-left: 5px;
  }

  .password-login a {
    text-decoration: none;
    color: black;
    font-weight: 600;
    align-self: flex-end;
    font-family: "Montserrat", sans-serif;
  }
</style>
