const sleep = (millis: number) => new Promise<void>(resolve => {
  setTimeout(() => {
    resolve(void (0))
  }, millis);
})

const run = async () => {
  await sleep(1000)
  console.log("hello")

  await sleep(2000)
  console.log("world")
}
