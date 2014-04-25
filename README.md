example:

- (BNWallet *)debugWalletFor:(NSString *)name
{
    BNWallet *wallet = [[BNWallet alloc] init];
    [wallet setPath:[@"/Users/richcollins/projects/OpenSource/Bitmarkets/Bitnash/sim/data/" stringByAppendingString:name]];
    wallet.server.logsStderr = YES;
    wallet.server.logsErrors = YES;
    return wallet;
}

- (void)showInsufficientValueError:(BNTx *)tx
{
    NSString *address = [tx.wallet createAddress];
    NSString *amount = [[[NSDecimalNumber decimalNumberWithDecimal:[tx.error.insufficientValue decimalValue]] decimalNumberByDividingBy:[NSDecimalNumber decimalNumberWithMantissa:1 exponent:8 isNegative:NO]] stringValue];
    
    NSString *paymentUrl = [NSString stringWithFormat:@"bitcoin:%@?amount=%@",
                            address,
                            amount];

    _imageView.image = [QRCodeGenerator qrImageForString:paymentUrl imageSize:256.0];
    
    [_textField setStringValue:[NSString stringWithFormat:@"Insufficient Value.  Please send %@ to %@", amount, address]];
}

- (void)debugEscrow
{
    BNEscrowTx *buyerTx = [[BNEscrowTx alloc] init];
    
    buyerTx.wallet = _buyerWallet;
    [buyerTx fillForValue:50000];
    
    if (buyerTx.error)
    {
        if (buyerTx.error.insufficientValue)
        {
            [self showInsufficientValueError:buyerTx];
        }
        return;
    }
    
    BNEscrowTx *sellerTx = [[BNEscrowTx alloc] init];
    sellerTx.wallet = _sellerWallet;
    [sellerTx fillForValue:50000];
    
    if (sellerTx.error)
    {
        if (sellerTx.error.insufficientValue)
        {
            [self showInsufficientValueError:sellerTx];
        }
        return;
    }
    
    _escrowTx = sellerTx;
    
    [_escrowTx mergeWithTx:buyerTx];
    
    [_escrowTx sign];
    
    _escrowTx.wallet = _buyerWallet;
    [_escrowTx sign];
    
    [_escrowTx broadcast];
    
    _escrowTx.wallet = _sellerWallet;
    [_escrowTx broadcast];
}

- (void)debugRelease
{
    self.releaseTx = [[BNReleaseTx alloc] init];
    _releaseTx.wallet = _sellerWallet;
    BNTxIn *txIn = [[BNTxIn alloc] init];
    txIn.previousOutIndex = [NSNumber numberWithInt:0];
    txIn.previousTxHash = _escrowTx.hash;
    
    [_releaseTx.inputs addObject:txIn];
    
    BNTxOut *txOut = [[BNTxOut alloc] init];
    txOut.value = [NSNumber numberWithLongLong:50000];
    
    BNPayToAddressScriptPubKey *scriptPubKey = [[BNPayToAddressScriptPubKey alloc] init];
    scriptPubKey.address = [_buyerWallet createAddress];
    txOut.scriptPubKey = scriptPubKey;
    
    [_releaseTx.outputs addObject:txOut];
    
    txOut = [[BNTxOut alloc] init];
    txOut.value = [NSNumber numberWithLongLong:50000];
    
    scriptPubKey = [[BNPayToAddressScriptPubKey alloc] init];
    scriptPubKey.address = [_sellerWallet createAddress];
    txOut.scriptPubKey = scriptPubKey;
    
    [_releaseTx.outputs addObject:txOut];
    
    [_releaseTx subtractFee];
    
    [_releaseTx sign];
    
    _releaseTx.wallet = _buyerWallet;
    [_releaseTx sign];
    
    [_releaseTx broadcast];
    
    _releaseTx.wallet = _sellerWallet;
    
    [_releaseTx broadcast];
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    self.buyerWallet = [self debugWalletFor:@"buyer"];
    self.sellerWallet = [self debugWalletFor:@"seller"];
    
    [_buyerWallet.server start];
    [_sellerWallet.server start];
    
    [self debugEscrow];
	//TODO wait for confirmation
    [self debugRelease];
}
