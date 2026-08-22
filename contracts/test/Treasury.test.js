const { expect } = require("chai");

describe("Treasury", function () {
  it("deploys and emits TransferRecorded", async function () {
    const [owner, addr1] = await ethers.getSigners();
    const Treasury = await ethers.getContractFactory("Treasury");
    const treasury = await Treasury.deploy(owner.address);
    await treasury.deployed();

    const tx = await treasury.connect(owner).recordTransfer(addr1.address, 100);
    const receipt = await tx.wait();
    const event = receipt.events.find((e) => e.event === "TransferRecorded");
    expect(event).to.not.be.undefined;
  });
});
