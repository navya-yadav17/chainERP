const { expect } = require("chai");

describe("Treasury", function () {
  async function deployTreasury() {
    const [owner, recipient, other] = await ethers.getSigners();
    const Treasury = await ethers.getContractFactory("Treasury");
    const treasury = await Treasury.deploy(owner.address);
    await treasury.waitForDeployment();
    return { treasury, owner, recipient, other };
  }

  it("settles an admin transfer and emits TransferRecorded", async function () {
    const { treasury, owner, recipient } = await deployTreasury();
    const treasuryAddress = await treasury.getAddress();
    await owner.sendTransaction({ to: treasuryAddress, value: 100n });

    const initialBalance = await ethers.provider.getBalance(recipient.address);
    await expect(treasury.connect(owner).transferTo(recipient.address, 100n))
      .to.emit(treasury, "TransferRecorded");

    expect(await ethers.provider.getBalance(recipient.address) - initialBalance).to.equal(100n);
    expect(await ethers.provider.getBalance(treasuryAddress)).to.equal(0n);
  });

  it("rejects transfers from non-admin accounts", async function () {
    const { treasury, recipient, other } = await deployTreasury();

    await expect(treasury.connect(other).transferTo(recipient.address, 1n))
      .to.be.revertedWith("not admin");
  });

  it("rejects invalid recipients", async function () {
    const { treasury, owner } = await deployTreasury();

    await expect(treasury.connect(owner).transferTo(ethers.ZeroAddress, 1n))
      .to.be.revertedWith("invalid recipient");
  });

  it("rejects transfers above the treasury balance", async function () {
    const { treasury, owner, recipient } = await deployTreasury();

    await expect(treasury.connect(owner).transferTo(recipient.address, 1n))
      .to.be.revertedWith("transfer failed");
  });
});
