<script>
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import GitHubIcon from "../assets/svg/github-logo.svg";
  import GitLabIcon from "../assets/svg/gitlab-logo.svg";
  import { addToast } from "./Modal/ToastNotification/toastStore.js";

  const backendUrl = __BACKEND_URL__;
  let showGithubOAuth;
  let showGitlabOAuth;
  let errorMessage = "";
  let errorMessageKey = "undefinedError";

  let status = "";

  onMount(async () => {
    const params = new URLSearchParams(window.location.search);
    errorMessage = params.get("exception");
    status = params.get("status");

    if (status === "NotAuthenticated") {
      addToast({ message: $_("login.toastNotifications.notAuthenticated") });
    }
    switch (errorMessage) {
      case "LockedException":
        errorMessageKey = "accountLocked";
        break;
      case "DisabledException":
        errorMessageKey = "accountDisabled";
        break;
      case "BadCredentialsException":
        errorMessageKey = "invalidCredentials";
        break;
      case "AccountExpiredException":
        errorMessageKey = "accountExpired";
        break;
    }

    try {
      const response = await fetch(backendUrl + "/api/loginOptions", {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      showGithubOAuth = data.github;
      showGitlabOAuth = data.gitlab;
    } catch (e) {
      console.error(e.message);
    }
  });

  const handleForgotPassword = () => {
    console.log("Forgot password clicked");
    addToast({ message: $_("login.toastNotifications.forgotPassword") });

    return fetch(backendUrl + `/api/user/forgotPassword`, {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
      body: "email@address",
    })
      .then((response) => {
        if (!response.ok) {
          addToast({
            message: message,
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        addToast({
          message: message,
          type: "error",
        });
        return null;
      });
  };
</script>

<div class="main-component">
  <h1>Studiosus</h1>
  <div class="login-component">
    {#if errorMessage}
      <h3 class="error">{$_(`login.errorMessages.${errorMessageKey}`)}</h3>
    {/if}
    {#if showGithubOAuth || showGitlabOAuth}
      <div class="oauth-logins">
        <div class="oauth-options">
          {#if showGithubOAuth}
            <a href={backendUrl + "/oauth2/authorization/github"}>
              <img src={GitHubIcon} alt="github logo" />{$_(
                `login.loginWithGithub`
              )}
            </a>
          {/if}
          {#if showGitlabOAuth}
            <a href={backendUrl + "/oauth2/authorization/gitlab"}>
              <img class="gitlab" src={GitLabIcon} alt="gitlab logo" />{$_(
                `login.loginWithGitLab`
              )}
            </a>
          {/if}
        </div>
      </div>
    {/if}
    <div class="password-login">
      <form action={backendUrl + "/login"} method="post">
        <input
          type="email"
          id="username"
          name="username"
          placeholder={$_("login.email")}
        />
        <input
          type="password"
          id="password"
          name="password"
          placeholder={$_("login.password")}
        />
        <div class="button-container">
          <button class="button--blue" type="submit">{$_("login.login")}</button
          >
        </div>
      </form>
      <a href="/forgot-password" class="forgot_password"
        >{$_("login.forgotPassword")}</a
      >
      <a href="/register">{$_("login.register")}</a>
    </div>
  </div>
</div>

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

  .oauth-logins,
  .password-login {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }

  .oauth-options,
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

  .oauth-options a {
    position: relative;
    text-decoration: none;
    color: black;
    padding: 10px 0px 10px 0px;
    margin: 5px 0 5px 0px;
    width: 90%;
    font-size: 1.5rem;
    background-color: var(--periwinkle);
    border: #000 1px solid;
    font-family: "Rubik", sans-serif;
  }

  .oauth-options a:hover {
    background-color: var(--spindle);
  }

  .oauth-options a img {
    position: absolute;
    padding: 0;
    margin: 0;
    width: 40px;
    top: 5px;
    left: 5px;
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

  .forgot_password {
    margin: 0 auto;
    font-size: 1rem;
    text-decoration: none;
    color: black;
    font-weight: 600;
    font-family: "Montserrat", sans-serif;
    outline: none;
    background: none;
    padding: 0;
    border: none;
  }
</style>
