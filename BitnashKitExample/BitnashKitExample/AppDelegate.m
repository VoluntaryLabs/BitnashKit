//
//  AppDelegate.m
//  BitnashKitExample
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "AppDelegate.h"
#import <QuartzCore/QuartzCore.h>
#import "QRCodeGenerator.h"

@implementation AppDelegate

//http://blockexplorer.com/testnet/tx/da81c21c8ea06feb3554f547090363e1b29626c4bdc4b1adc19e95e37bf32cca

- (BNWallet *)debugWalletFor:(NSString *)name
{
    BNWallet *wallet = [[BNWallet alloc] init];
    
    NSString *dataPath = [[@"~/Library/Application Support/BitnashKit/" stringByExpandingTildeInPath] stringByAppendingPathComponent:name];
    
    NSError *error;
    [[NSFileManager defaultManager] createDirectoryAtPath:dataPath withIntermediateDirectories:YES attributes:nil error:&error];
    
    [wallet setPath:dataPath];
    wallet.server.logsStderr = YES;
    //wallet.server.logsErrors = YES;
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
    BNTx *buyerTx = [[BNTx alloc] init];
    buyerTx.wallet = _buyerWallet;
    [buyerTx configureForEscrowWithValue:50000];
    [buyerTx writeToFile:@"/tmp/buyer-escrow-tx.json"];
    
    if (buyerTx.error)
    {
        if (buyerTx.error.insufficientValue)
        {
            [self showInsufficientValueError:buyerTx];
        }
        
        return;
    }
    
    BNTx *sellerTx = [[BNTx alloc] init];
    sellerTx.wallet = _sellerWallet;
    [sellerTx configureForEscrowWithValue:50000];
    [sellerTx writeToFile:@"/tmp/seller-escrow-tx.json"];
    
    if (sellerTx.error)
    {
        if (sellerTx.error.insufficientValue)
        {
            [self showInsufficientValueError:sellerTx];
        }
        
        return;
    }
    
    _escrowTx = [sellerTx mergedWithEscrowTx:buyerTx];
    
    [_escrowTx writeToFile:@"/tmp/escrow-tx-merged.json"];
    
    [_escrowTx subtractFee];
    
    [_escrowTx writeToFile:@"/tmp/escrow-tx-with_fees.json"];
    
    [_escrowTx sign];
    
    [_escrowTx writeToFile:@"/tmp/escrow-tx-seller-signed.json"];
    
    _escrowTx.wallet = _buyerWallet;
    [_escrowTx sign];
    
    [_escrowTx writeToFile:@"/tmp/escrow-tx-fully-signed.json"];
    
    BNTx *cancellationTx = [_escrowTx cancellationTx];
    [cancellationTx writeToFile:@"/tmp/cancellation.json"];
    
    return;
    
    [_escrowTx broadcast];
    
    _escrowTx.wallet = _sellerWallet;
    [_escrowTx broadcast];
}

- (void)debugRelease
{
    self.releaseTx = [[BNTx alloc] init];
    _releaseTx.wallet = _sellerWallet;
    BNTxIn *txIn = [[BNTxIn alloc] init];
    txIn.previousOutIndex = [NSNumber numberWithInt:0];
    txIn.previousTxHash = @"1f0a6a48812473fd7a63aa675e7f87b0a983f26c37f24f95e09052ca658cec6d";
    
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
    
    //http://testnet.btclook.com/txn/bd9f6215244f1059fdf853c2fd01f96ffa53aef6efc77846e4064cc904bf012b
    //bd9f6215244f1059fdf853c2fd01f96ffa53aef6efc77846e4064cc904bf012b
}

- (void)showBalances
{
    NSLog(@"\n\nbuyer balance: %@\n\n", [_buyerWallet balance]);
    NSLog(@"\n\nseller balance: %@\n\n", [_sellerWallet balance]);
}

- (void)showStatus
{
    NSLog(@"%@", [_buyerWallet.server status]);
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    //TODO: Should we delegate the state for BNObjects to a BitcoinJ object?
    
    
    self.buyerWallet = [self debugWalletFor:@"buyer"];
    //self.sellerWallet = [self debugWalletFor:@"seller"];
    
    [self showStatus];
    [self performSelector:@selector(showStatus) withObject:nil afterDelay:5];
    
    //[self showBalances];
    
    //[self debugEscrow];
    //[self debugRelease];
}

@end
