<script>
  import { Route, Router } from "svelte-routing";
  import CodeEditor from "./Components/Editor/CodeEditor.svelte";
  import Footer from "./Components/Footer.svelte";
  import Navbar from "./Components/Navbar.svelte";
</script>

<Navbar />
<main>
  <div class="main-component">
    <h1>Studiosus</h1>
    <p>Universal Ansible Editor</p>
    <Router>
      <Route path="/" let:params component={CodeEditor} />
      <Route path="*" let:params component={CodeEditor} />
    </Router>
  </div>
</main>
<Footer />

<style>
  main {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
  }

  .main-component {
    transition: all 0.3s ease;
    position: relative;
    padding: 15px;
    border-radius: 5%;
    border: 3px dashed #000;
  }

  .main-component:hover {
    transform: translateY(-2px) scale(1.01);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .main-component p {
    margin-top: 5px;
    max-width: max-content;
    white-space: nowrap;
    overflow: hidden;
    border-right: 0.15em solid #000; /* The typwriter cursor */
    /* add typing & typewriter cursor effect */
    animation:
      typing 2s steps(40, end) forwards,
      blink-caret 0.75s step-end 2s infinite;
  }

  /* The typing effect */
  @keyframes typing {
    from {
      width: 0;
    }
    to {
      width: 100%;
    }
  }

  /* The typewriter cursor effect */
  @keyframes blink-caret {
    from,
    to {
      border-color: transparent;
    }
    50% {
      border-color: #000;
    }
  }

  /* On smaller devices, set main component width to 90% */
  @media (max-width: 900px) {
    .main-component {
      width: 90%;
    }
  }
</style>
