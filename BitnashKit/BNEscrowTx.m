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
            @"hash",
            @"isLocked",
            nil];
}

- (void)fillForValue:(long long)value
{
    BNEscrowTx *tx = [self.wallet.server sendMessage:@"fillEscrowTx" withObject:[NSNumber numberWithLongLong:value]];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    BNMultisigScriptPubKey *scriptPubKey = (BNMultisigScriptPubKey *)[self multisigOutput].scriptPubKey;
    [scriptPubKey.pubKeys removeLastObject];
}

- (BNTxOut *)multisigOutput
{
    for (BNTxOut *txOut in self.outputs)
    {
        if ([txOut.scriptPubKey isMultisig])
        {
            return txOut;
        }
    }
    return nil;
}

- (void)addInputsFromTx:(BNTx *)tx;
{
    [self.inputs addObjectsFromArray:tx.inputs];
}

- (void)mergeWithTx:(BNTx *)tx
{
    [self addInputsFromTx:tx];
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
            [self.outputs addObject:txOut];
        }
    }
}

- (void)sign
{
    BNEscrowTx *tx = [self.wallet.server sendMessage:@"signEscrowTx" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

- (void)broadcast
{
    BNEscrowTx *tx = [self.wallet.server sendMessage:@"broadcast" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

@end
