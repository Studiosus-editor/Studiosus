<script>
  import { _ }  from 'svelte-i18n';
  import { onMount } from "svelte";

  let errorMessage = "";
  let errorMessageKey = "undefinedError"

  onMount(async () => {
    const params = new URLSearchParams(window.location.search);
    errorMessage = params.get("exception");
    if (errorMessage === 'IllegalArgumentException') {
      errorMessageKey = 'emailAlreadyInUse';
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
        <form action="/register" method="post">
          <input type="text" name="username" placeholder={$_('register.username')} required />
          <input type="email" name="email" placeholder={$_('register.email')} required />
          <input
            type="password"
            name="password"
            placeholder={$_('register.password')}
            required
          />
          <button type="submit">{$_('register.register')}</button>
        </form>
        <a href="/login">{$_('register.login')}</a>
      </div>
    </div>
  </div>
</main>

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
    font-weight: bold;
    font-weight: 600;
    align-self: flex-end;
    font-family: "Montserrat", sans-serif;
  }
</style>
