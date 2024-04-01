<script>
  import { onMount } from "svelte";
  import GitHubIcon from "../assets/svg/github-logo.svg";
  import GitLabIcon from "../assets/svg/gitlab-logo.svg";
  import { _ }  from 'svelte-i18n';

  let showGithubOAuth;
  let showGitlabOAuth;
  let errorMessage = "";
  let errorMessageKey = "undefinedError"
  
  onMount(async () => {
    const params = new URLSearchParams(window.location.search);
    errorMessage = params.get("exception");
    
    switch(errorMessage) {
      case 'LockedException':
        errorMessageKey = 'accountLocked';
        break;
      case 'DisabledException':
        errorMessageKey = 'accountDisabled';
        break;
      case 'BadCredentialsException':
        errorMessageKey = 'invalidCredentials';
        break;
      case 'AccountExpiredException':
        errorMessageKey = 'accountExpired';
        break;
  }

    try {
      const response = await fetch("/api/loginOptions", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
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
            <a href="/oauth2/authorization/github">
              <img src={GitHubIcon} alt="github logo" />Log in with GitHub
            </a>
          {/if}
          {#if showGitlabOAuth}
            <a href="/oauth2/authorization/gitlab">
              <img class="gitlab" src={GitLabIcon} alt="gitlab logo" />Log in
              with GitLab
            </a>
          {/if}
        </div>
      </div>
    {/if}
    <div class="password-login">
      <form action="/login" method="post">
        <input type="email" id="username" name="username" placeholder={$_('login.email')} />
        <input
          type="password"
          id="password"
          name="password"
          placeholder={$_('login.password')}
        />
        <button type="submit">{$_('login.login')}</button>
      </form>
      <a href="/register">{$_('login.register')}</a>
    </div>
  </div>
</div>

<style>
  .main-component {
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.5);
    text-align: center;
    width: 400px;
    transition: all 0.3s ease;
    position: relative;
    padding: 15px;
    border-radius: 5%;
    border: 2px solid #000;
    background-color:  var(--grey85);
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

  form button {
    border-radius: 25px;
    margin-top: 10px;
    width: 50%;
    height: 30px;
    border: #000 1px solid;
    font-family: "Rubik", sans-serif;
    background-color: var(--periwinkle);
    box-shadow:  0px 8px 8px 0px rgba(0, 0, 0, 0.3);
    font-size: 16px;
  }

  button:hover {
    cursor: pointer;
    background-color: var(--spindle); 
  }

  .password-login a {
    text-decoration: none;
    color: black;
    font-weight: 600;
    align-self: flex-end;
    font-family: "Montserrat", sans-serif;
  }
</style>
