<script>
  import { onMount } from "svelte";

  let showGithubOAuth;
  let showGitlabOAuth;

  onMount(async () => {
    try {
      const response = await fetch("http://localhost:8080/api/loginOptions", {
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
  <p>Studiosus</p>
  <div class="login-component">
    {#if showGithubOAuth || showGitlabOAuth}
      <div class="oauth-logins">
        <div class="oauth-options">
          {#if showGithubOAuth}
            <a href="/oauth2/authorization/github">
              <img src="icons/github-logo.png" alt="github logo" />Log in with
              GitHub
            </a>
          {/if}
          {#if showGitlabOAuth}
            <a href="/oauth2/authorization/gitlab">
              <img
                class="gitlab"
                src="icons/gitlab-logo.png"
                alt="gitlab logo"
              />Log in with GitLab
            </a>
          {/if}
        </div>
      </div>
    {/if}
    <div class="password-login">
      <form action="" method="post">
        <input
          type="email"
          id="email-field"
          name="email-field"
          placeholder="Email"
        />
        <input
          type="password"
          id="password-field"
          name="password-field"
          placeholder="Password"
        />
        <button type="submit" disabled>Login</button>
      </form>
      <a href="/register">Register</a>
    </div>
  </div>
</div>

<style>
  .main-component {
    text-align: center;
    width: 400px;
    transition: all 0.3s ease;
    position: relative;
    padding: 15px;
    border-radius: 5%;
    border: 3px dashed #000;
    background-color: rgb(251, 242, 198);
  }

  .main-component p {
    font-size: 2rem;
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
    height: 50px;
    font-size: 1.5rem;
    background-color: rgb(217, 217, 217);
    border: #000 1px solid;
  }

  .oauth-options a:hover {
    background-color: rgb(197, 196, 196);
  }

  .oauth-options a img {
    position: absolute;
    padding: 0;
    margin: 0;
    width: 40px;
    top: 5px;
    left: 5px;
  }

  .oauth-options a img.gitlab {
    top: -10px;
    left: -10px;
    width: 70px;
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
    margin-top: 10px;
    width: 50%;
    height: 30px;
    /* disalbe default button styles */
    border: #000 1px solid;
    text-decoration: none;
    background-color: rgb(196, 203, 250);
    font-size: 16px;
  }

  .password-login a {
    text-decoration: none;
    color: black;
    font-weight: bold;
    align-self: flex-end;
  }
</style>
