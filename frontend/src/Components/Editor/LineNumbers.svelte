<script>
  export let textareaValue; // Accept textareaValue as a prop

  // Reactive statement to generate line numbers whenever textareaValue changes
  $: lineNumbers = Array.from(
    { length: $textareaValue.split("\n").length },
    (_, i) => i + 1
  );
</script>

<div id="line-numbers">
  {#each lineNumbers as lineNumber (lineNumber)}
    <span>{lineNumber}</span>
  {/each}
</div>

<style>
  /* Container that displays the line numbers in the code editor. */
  #line-numbers {
    background: #2b2b2b;
    width: 23px;
    padding: 0 15px;
    height: 100%;
    line-height: 21px;
  }

  /* Each span is responsbile for 1 line number field */
  #line-numbers span {
    counter-increment: linenumber; /* Increment the counter */
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
</style>
