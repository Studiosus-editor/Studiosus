<script>
  let errorMessage = "";

  const params = new URLSearchParams(window.location.search);
  errorMessage = params.get("error");
</script>

<main>
  <div class="main-component">
    <p>Studiosus</p>
    <div class="login-component">
      {#if errorMessage}
        <p class="error">{errorMessage}</p>
      {/if}
      <div class="password-login">
        <form action="/register" method="post">
          <input type="text" name="username" placeholder="Username" required />
          <input type="email" name="email" placeholder="Email" required />
          <input
            type="password"
            name="password"
            placeholder="Password"
            required
          />
          <button type="submit">Register</button>
        </form>
        <a href="/login">Login</a>
      </div>
    </div>
  </div>
</main>

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

  .main-component .error {
    font-family: "Courier New", Courier, monospace;
    font-size: 1.5rem;
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
