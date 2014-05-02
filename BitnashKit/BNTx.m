//
//  BNTx.m
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTx.h"
#import "BNWallet.h"
#import "BNMultisigScriptPubKey.h"
#import "BNPayToAddressScriptPubKey.h"

@implementation BNTx

- (id)init
{
    self = [super init];
    self.inputs = [NSMutableArray array];
    self.outputs = [NSMutableArray array];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"error",
                                                   @"inputs",
                                                   @"outputs",
                                                   @"hash",
                                                   @"netValue",
                                                   @"updateTime",
                                                   @"counterParty",
                                                   nil]];
    return self;
}

- (id)sendToServer:(NSString *)message withArg:(id)arg
{
    id result = [_wallet.server sendMessage:message withObject:self withArg:arg];
    self.error = _wallet.server.error;
    return result;
}

- (id)sendToServer:(NSString *)message
{
    return [self sendToServer:message withArg:nil];
}

- (BNTxOut *)newOutput
{
    BNTxOut *newOutput = [[BNTxOut alloc] init];
    [_outputs addObject:newOutput];
    return newOutput;
}

- (void)configureForEscrowWithValue:(long long)value
{
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = [NSNumber numberWithLongLong:value];
    
    BNMultisigScriptPubKey *script = [[BNMultisigScriptPubKey alloc] init];
    BNKey *key = [_wallet createKey];
    [script.pubKeys addObject:key.pubKey];
    [script.pubKeys addObject:key.pubKey]; //do it twice to properly estimate tx size for fees
    txOut.scriptPubKey = script;
    
    [self copySlotsFrom:[self sendToServer:@"addInputsAndChange"]];
}

- (void)subtractFee
{
    [self copySlotsFrom:[self sendToServer:@"subtractFee"]];
}

- (BNMultisigScriptPubKey *)multisigScriptPubKey
{
    return (BNMultisigScriptPubKey *)([self multisigOutput].scriptPubKey);
}

- (BNTx *)mergedWithEscrowTx:(BNTx *)tx
{
    BNTx *mergedTx = [[BNTx alloc] init];
    mergedTx.wallet = _wallet;
    
    [mergedTx.inputs addObjectsFromArray:_inputs];
    [mergedTx.inputs addObjectsFromArray:tx.inputs];
    
    [mergedTx.outputs addObjectsFromArray:_outputs];
    [mergedTx.outputs addObjectsFromArray:tx.outputs];
    
    while ([mergedTx multisigOutput])
    {
        [mergedTx.outputs removeObject:[mergedTx multisigOutput]];
    }
    
    BNTxOut *newMultisigOut = [mergedTx newOutput];
    newMultisigOut.value = [NSNumber numberWithLongLong:[self multisigOutput].value.longLongValue + [tx multisigOutput].value.longLongValue];
    
    BNMultisigScriptPubKey *multisigScriptPubKey = [[BNMultisigScriptPubKey alloc] init];
    [multisigScriptPubKey.pubKeys addObject:[[self multisigScriptPubKey].pubKeys firstObject]];
    [multisigScriptPubKey.pubKeys addObject:[[tx multisigScriptPubKey].pubKeys firstObject]];
    
    newMultisigOut.scriptPubKey = multisigScriptPubKey;
     
    return mergedTx;
}

- (void)sign
{
    [self copySlotsFrom:[self sendToServer:@"sign"]];
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

- (void)broadcast
{
    [self sendToServer:@"broadcast"];
}

- (BOOL)isConfirmed
{
    return [(NSNumber *)[self sendToServer:@"isConfirmed"] boolValue];
}

- (void)markInputsAsSpent
{
    [self sendToServer:@"markInputsAsSpent"];
}

- (void)markInputsAsUnspent
{
    [self sendToServer:@"markInputsAsUnspent"];
}

- (BNTx *)cancellationTx
{
    BNTx *tx = [self sendToServer:@"removeForeignInputs"];
    tx.wallet = _wallet;
    [tx.outputs removeAllObjects];
    
    BNTxOut *txOut = [tx newOutput];
    txOut.value = [tx sendToServer:@"inputValue"];
    
    BNPayToAddressScriptPubKey *scriptPubKey = [[BNPayToAddressScriptPubKey alloc] init];
    scriptPubKey.address = [_wallet createKey].address;
    txOut.scriptPubKey = scriptPubKey;
    
    [tx subtractFee];
    
    return tx;
}

@end
