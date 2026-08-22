async function main() {
  const [deployer] = await ethers.getSigners();
  console.log("Deploying contracts with:", deployer.address);

  const Treasury = await ethers.getContractFactory("Treasury");
  const treasury = await Treasury.deploy(deployer.address);
  await treasury.deployed();
  console.log("Treasury deployed to:", treasury.address);

  const Payroll = await ethers.getContractFactory("Payroll");
  const payroll = await Payroll.deploy();
  await payroll.deployed();
  console.log("Payroll deployed to:", payroll.address);

  const SubscriptionManager = await ethers.getContractFactory("SubscriptionManager");
  const subscription = await SubscriptionManager.deploy();
  await subscription.deployed();
  console.log("SubscriptionManager deployed to:", subscription.address);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
