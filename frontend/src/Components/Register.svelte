<script>
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";
  const backendUrl = __BACKEND_URL__;
  let errorMessage = "";
  let errorMessageKey = "undefinedError";

  onMount(async () => {
    const params = new URLSearchParams(window.location.search);
    errorMessage = params.get("exception");
    if (errorMessage === "IllegalArgumentException") {
      errorMessageKey = "emailAlreadyInUse";
    } else if (errorMessage === "UsernameTooLong") {
      errorMessageKey = "usernameTooLong";
    } else if (errorMessage === "EmailTooLong") {
      errorMessageKey = "emailTooLong";
    } else if (errorMessage === "PasswordTooLong") {
      errorMessageKey = "passwordTooLong";
    }
  });
</script>

<main>
  <div class="main-component">
    <h1>Studiosus</h1>
    <div class="login-component">
      {#if errorMessage}
        <h3 class="error">{$_(`register.errorMessages.${errorMessageKey}`)}</h3>
      {/if}
      <div class="password-login">
        <form action={backendUrl + "/register"} method="post">
          <input
            type="text"
            name="username"
            placeholder={$_("register.username")}
            required
            maxlength="64"
          />
          <input
            type="email"
            name="email"
            placeholder={$_("register.email")}
            required
            maxlength="64"
          />
          <input
            type="password"
            name="password"
            placeholder={$_("register.password")}
            required
            maxlength="64"
          />
          <div class="button-container">
            <button class="button--blue" type="submit"
              >{$_("register.register")}</button
            >
          </div>
        </form>
        <a href="/login">{$_("register.login")}</a>
      </div>
    </div>
  </div>
</main>

<style>
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

  .main-component .error {
    color: red;
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
    font-weight: bold;
    font-weight: 600;
    align-self: flex-end;
    font-family: "Montserrat", sans-serif;
  }
</style>
