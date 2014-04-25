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
            BNTxOut *multisigOutput = [self multisigOutput];
            
            multisigOutput.value = [NSNumber numberWithLongLong:[multisigOutput.value longLongValue] + [txOut.value longLongValue]];
            
            BNMultisigScriptPubKey *multisigScriptPubKey = (BNMultisigScriptPubKey *)multisigOutput.scriptPubKey;
            BNMultisigScriptPubKey *txOutScriptPubKey = (BNMultisigScriptPubKey *)(txOut.scriptPubKey);
            
            [multisigScriptPubKey.pubKeys removeLastObject];
            [multisigScriptPubKey.pubKeys addObject:[txOutScriptPubKey.pubKeys firstObject]];
        }
        else
        {
            [self.outputs addObject:txOut];
        }
    }
}

- (void)broadcast
{
    BNEscrowTx *tx = [self.wallet.server sendMessage:@"broadcast" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

@end
