//
//  BNEscrowTransaction.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNEscrowTx.h"
#import "BNTxIn.h"
#import "BNTxOut.h"
#import "NSObject+BNJSON.h"
#import "BNWallet.h"
#import "BNMultisigScriptPubKey.h"

@implementation BNEscrowTx

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:
            @"inputs",
            @"outputs",
            nil];
}

- (NSNumber *)fee
{
    return nil;
}

- (void)fillForValue:(long long)value
{
    BNEscrowTx *tx = [_wallet.server sendMessage:@"fillEscrowTx" withObject:[NSNumber numberWithLongLong:value]];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    BNMultisigScriptPubKey *scriptPubKey = (BNMultisigScriptPubKey *)[self multisigOutput].scriptPubKey;
    [scriptPubKey.pubKeys removeLastObject];
}

- (BNTxOut *)multisigOutput
{
    for (BNTxOut *txOut in _outputs)
    {
        if ([txOut.scriptPubKey isMultisig])
        {
            return txOut;
        }
    }
    return nil;
}

- (void)mergeWithEscrowTx:(BNEscrowTx *)tx
{
    [_inputs addObjectsFromArray:tx.inputs];
    for (BNTxOut *txOut in tx.outputs)
    {
        if ([txOut.scriptPubKey isMultisig])
        {
            [self multisigOutput].value = [NSNumber numberWithLongLong:[[self multisigOutput].value longLongValue] + [txOut.value longLongValue]];
            
            BNMultisigScriptPubKey *myScriptPubKey = (BNMultisigScriptPubKey *)([self multisigOutput].scriptPubKey);
            BNMultisigScriptPubKey *txOutScriptPubKey = (BNMultisigScriptPubKey *)(txOut.scriptPubKey);
            [myScriptPubKey.pubKeys addObjectsFromArray:txOutScriptPubKey.pubKeys];
        }
        else
        {
            [_outputs addObject:txOut];
        }
    }
}

- (void)addFeeAndSign
{
    BNEscrowTx *tx = [_wallet.server sendMessage:@"addFeeAndSignEscrowTx" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
}

@end
