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
    
    NSString *dataPath = [@"~/Library/Application Support/BitnashKit/" stringByExpandingTildeInPath];
    NSError *error;
    [[NSFileManager defaultManager] createDirectoryAtPath:dataPath withIntermediateDirectories:YES attributes:nil error:&error];
    
    [wallet setPath:dataPath];
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
    
    /*
    CIFilter *qrFilter = [CIFilter filterWithName:@"CIQRCodeGenerator"];
    [qrFilter setValue:[paymentUrl dataUsingEncoding:NSUTF8StringEncoding] forKey:@"inputMessage"];
    [qrFilter setValue:@"H" forKey:@"inputCorrectionLevel"];
    
    CGAffineTransform xForm = CGAffineTransformMakeScale(5.0f, 5.0f);
    [qrFilter setValue:[NSValue valueWithBytes:&xForm objCType:@encode(CGAffineTransform)] forKey:@"inputTransform"];
    
    [qrFilter setValue:kCISamplerFilterLinear forKey:kCISamplerFilterMode];
    
    CIImage *ciImage = [qrFilter valueForKey:kCIOutputImageKey];
    
    //ciImage = [ciImage imageByApplyingTransform:CGAffineTransformMakeScale(5.0f, 5.0f)];
    
    NSCIImageRep *rep = [NSCIImageRep imageRepWithCIImage:ciImage];
    NSImage *nsImage = [[NSImage alloc] initWithSize:rep.size];
    [nsImage addRepresentation:rep];
     */
    
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
    [buyerTx writeToFile:@"/Users/richcollins/Downloads/escrow-filled-buyer.json"];
    
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
    [sellerTx writeToFile:@"/Users/richcollins/Downloads/escrow-filled-seller.json"];
    
    _escrowTx = sellerTx;
    
    [_escrowTx mergeWithTx:buyerTx];
    
    [_escrowTx sign];
    [_escrowTx writeToFile:@"/Users/richcollins/Downloads/escrow-signed-seller.json"];
    
    _escrowTx.wallet = _buyerWallet;
    [_escrowTx sign];
    [_escrowTx writeToFile:@"/Users/richcollins/Downloads/escrow-signed-buyer.json"];
    
    [_escrowTx broadcast];
    [_escrowTx writeToFile:@"/Users/richcollins/Downloads/escrow-broadcasted-buyer.json"];
    
    _escrowTx.wallet = _sellerWallet;
    [_escrowTx broadcast];
    [_escrowTx writeToFile:@"/Users/richcollins/Downloads/escrow-broadcasted-seller.json"];
}

- (void)debugRelease
{
    self.releaseTx = [[BNReleaseTx alloc] init];
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
    
    [_releaseTx writeToFile:@"/Users/richcollins/Downloads/release-unsigned.json"];
    
    [_releaseTx subtractFee];
    
    [_releaseTx writeToFile:@"/Users/richcollins/Downloads/release-with-fees.json"];
    
    [_releaseTx sign];
    
    [_releaseTx writeToFile:@"/Users/richcollins/Downloads/release-signed-seller.json"];
    
    _releaseTx.wallet = _buyerWallet;
    [_releaseTx sign];
    
    [_releaseTx writeToFile:@"/Users/richcollins/Downloads/release-signed-buyer.json"];
    
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

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    //TODO: Should we delegate the state for BNObjects to a BitcoinJ object?
    
    
    self.buyerWallet = [self debugWalletFor:@"buyer"];
    self.sellerWallet = [self debugWalletFor:@"seller"];
    
    [_buyerWallet.server start];
    [_sellerWallet.server start];
    
    //[NSTimer scheduledTimerWithTimeInterval:10 target:self selector:@selector(showBalances) userInfo:nil repeats:YES];
    
    [self showBalances];
    
    //[self debugEscrow];
    //[self debugRelease];
}

@end
