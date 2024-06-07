<script>
  import { _ } from "svelte-i18n";
  import { addToast } from "./Modal/ToastNotification/toastStore.js";

  export let params = null;
  const backendUrl = __BACKEND_URL__;

  let password = "";
  let repeatedPassword = "";

  async function handleSubmit(event) {
    event.preventDefault();

    if (password === "") {
      addToast({
        message: $_("resetPassword.toastNotifications.emptyPassword"),
        type: "error",
      });
      return;
    }

    const response = await fetch(
      backendUrl + `/api/reset-password/${params.resetToken}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: password,
      }
    );

    if (response.status === 200) {
      addToast({
        message: $_("resetPassword.toastNotifications.success"),
        type: "success",
      });
      //   redirect to login page after 5s
      setTimeout(() => {
        location.href = "/login";
      }, 5000);
    } else {
      addToast({
        message: $_("resetPassword.toastNotifications.error"),
        type: "error",
      });
    }
  }
</script>

<div class="main-component">
  <h1>Studiosus</h1>
  <div class="login-component">
    <div class="password-login">
      <form on:submit={handleSubmit}>
        <input
          type="password"
          id="password"
          name="password"
          placeholder={$_("login.password")}
          bind:value={password}
        />
        <input
          type="password"
          id="repeatPassword"
          name="repeatPassword"
          placeholder={$_("login.repeatPassword")}
          bind:value={repeatedPassword}
        />
        <div class="button-container">
          <button
            disabled={password !== repeatedPassword || password === ""}
            class="button--blue"
            type="submit">{$_("forgotPassword.resetPassword")}</button
          >
        </div>
      </form>
      <a href="/login">{$_("forgotPassword.goBack")}</a>
    </div>
  </div>
</div>

<style>
  button:disabled {
    opacity: 0.5;
    color: black;
    cursor: default;
  }

  button:disabled:hover {
    background-color: var(--periwinkle);
  }

  .button-container {
    margin-top: 24px;
    width: 50%;
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
