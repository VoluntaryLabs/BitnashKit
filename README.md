example:

BNWallet *buyer = [[BNWallet alloc] init];
BNEscrowTx *buyerTx = [wallet newEscrowTx];
[buyerTx fillForValue:50000];

BNWallet *seller = [[BNWallet alloc] init];
BNEscrowTx *sellerTx = [wallet newEscrowTx];
[sellerTx fillForValue:50000];

[buyerTx mergeWithEscrowTx:sellerTx];

[buyerTx addFeeAndSign]; //TODO
buyerTx.wallet = sellerWallet;
[buyerTx addFeeAndSign]; //TODO

[buyerTx broadcast]; //TODO