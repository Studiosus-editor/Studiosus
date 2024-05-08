<script>
  export let textareaValue;

  // Reactive statement to generate line numbers whenever textareaValue changes
  $: lineNumbers = Array.from(
    { length: Math.max(1, $textareaValue.split("\n").length - 1) },
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
    line-height: 23px;
    padding-left: 8px;
    padding-top: 3px;
  }

  #line-numbers span {
    padding-right: 8px;
    counter-increment: linenumber; /* Increment the counter */
    display: flex;
    justify-content: right;
    font-size: 18px;
    color: var(--grey43);
    background-color: transparent;
  }
</style>
