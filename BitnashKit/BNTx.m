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
#import "BNTxIn.h"

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
                                                   @"txHash",
                                                   @"netValue",
                                                   @"fee",
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

- (BNTxIn *)newInput
{
    BNTxIn *newInput = [[BNTxIn alloc] init];
    [_inputs addObject:newInput];
    return newInput;
}

- (BNTxOut *)newOutput
{
    BNTxOut *newOutput = [[BNTxOut alloc] init];
    [_outputs addObject:newOutput];
    return newOutput;
}

- (void)configureForOutputWithValue:(NSNumber *)value
{
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = value;
    
    BNPayToAddressScriptPubKey *script = [[BNPayToAddressScriptPubKey alloc] init];
    BNKey *key = [_wallet createKey];
    script.address = key.address;
    txOut.scriptPubKey = script;
    
    [self copySlotsFrom:[self sendToServer:@"addInputsAndChange"]];
}

- (void)configureForEscrowWithValue:(NSNumber *)value
{
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = value;
    
    BNMultisigScriptPubKey *script = [[BNMultisigScriptPubKey alloc] init];
    BNKey *key = [_wallet createKey];
    [script.pubKeys addObject:key.pubKey];
    [script.pubKeys addObject:key.pubKey]; //do it twice to properly estimate tx size for fees
    txOut.scriptPubKey = script;
    
    [self copySlotsFrom:[self sendToServer:@"addInputsAndChange"]];
}

- (void)configureForEscrowWithInputTx:(BNTx *)inputTx
{
    BNTxIn *txIn = [self newInput];
    txIn.previousOutIndex = [NSNumber numberWithInt:0];
    txIn.previousTxHash = inputTx.txHash;
    
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = [(BNTxOut *)[inputTx.outputs firstObject] value];
    
    BNMultisigScriptPubKey *script = [[BNMultisigScriptPubKey alloc] init];
    BNKey *key = [_wallet createKey];
    [script.pubKeys addObject:key.pubKey];
    [script.pubKeys addObject:key.pubKey]; //do it twice to properly estimate tx size for fees
    txOut.scriptPubKey = script;
}

- (void)configureForEscrowSpendingOutput:(BNTxOut *)utxo
{
    [self configureForEscrowWithValue:utxo.value];
    
    BNTxIn *txIn = [[BNTxIn alloc] init];
    [txIn configureFromTxOut:utxo];
    [_inputs addObject:txIn];
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

- (BNTxIn *)multisigInput
{
    for (BNTxIn *txIn in self.inputs)
    {
        if ([[txIn.scriptSig isMultisig] boolValue])
        {
            return txIn;
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

- (NSString *)txTypeString //TODO -- more rigorous heuristic
{
    if (self.netValue.longLongValue < 0)
    {
        if ([self multisigOutput])
        {
            return @"Escrow";
        }
        else
        {
            return @"Withdrawal";
        }
    }
    else if ([self multisigInput])
    {
        return @"Escrow Release";
    }
    else
    {
        return @"Deposit";
    }
}

- (NSString *)nodeSubtitle
{
    return self.txHash;
}

- (NSString *)nodeNote
{
    return nil;
}

- (NSString *)nodeTitle
{
    /*
    if (self.children.count == 0)
    {
        [self composeChildrenFromPropertyNames:@[@"updateTime", @"counterParty"]];
    }
    */
    
    return [NSString stringWithFormat:@"%@ of %.4f BTC", self.txTypeString, (float)(self.netValue.doubleValue * 0.00000001)];
}

- (NSUInteger)hash
{
    return [self.txHash hash];
}

- (BOOL)isEqualTo:(id)object
{
    return [self.txHash isEqualTo:[object performSelector:@selector(txHash)]];
}

- (NSString *)webUrl
{
    return [@"http://testnet.btclook.com/txn/" stringByAppendingString:self.txHash];
}

- (BNTxOut *)changeOutput
{
    if (self.outputs.count > 1)
    {
        return [self.outputs lastObject];
    }
    else
    {
        return nil;
    }
}

- (NSNumber *)changeValue
{
    if ([self changeOutput])
    {
        return [self changeOutput].value;
    }
    else
    {
        return [NSNumber numberWithLongLong:0];
    }
}

@end
