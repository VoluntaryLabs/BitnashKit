example:

//buyer

BNWallet *buyer = [[BNWallet alloc] init];
BNEscrowTx *buyerTx = [wallet newEscrowTx];
[buyerTx fillForValue:100000];

//get seller inputs and outputs
BNEscrowTx *sellerTx = [[self receiveFromPeer] asObjectFromJSONString];

[buyerTx mergeWithEscrowTx:sellerTx];

[buyerTx addFee];

[buyerTx sign];

//send to seller for signature and broadcast
[self sendToPeer:[buyerTx asJSONString]];

//seller

BNWallet *seller = [[BNWallet alloc] init];
BNEscrowTx *sellerTx = [wallet newEscrowTx];
[sellerTx fillForValue:100000];

[self sendToPeer:[sellerTx asJSONString]];

BNEscrowTx *buyerTx = [[self receiveFromPeer] asObjectFromJSONString];
buyerTx.wallet = seller;
[buyerTx sign];
[buyerTx broadcast];