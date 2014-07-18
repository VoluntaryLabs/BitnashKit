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

@synthesize description = _description;
@synthesize txType = _txType;

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
                                                   @"serializedHex",
                                                   @"netValue",
                                                   @"fee",
                                                   @"updateTime",
                                                   @"counterParty",
                                                   @"confirmations",
                                                   nil]];
    _description = @"Unknown";
    _txType = @"Unknown";
    self.nodeViewClass = NavDescriptionView.class;
    return self;
}

- (id)descriptionJSONObject
{
    return self.asJSONObject;
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
    [self addPayToAddressOutputWithValue:value];
    
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
    
    [self addInputsAndChange];
}

- (void)configureForEscrowWithInputTx:(BNTx *)inputTx
{
    if (inputTx.subsumingTx) {
        inputTx = inputTx.subsumingTx;
    }
    
    BNTxIn *txIn = [self newInput];
    txIn.previousOutIndex = [NSNumber numberWithInt:0];
    txIn.previousTxHash = inputTx.txHash;
    txIn.previousTxSerializedHex = inputTx.serializedHex;
    
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = [(BNTxOut *)[inputTx.outputs firstObject] value];
    
    BNMultisigScriptPubKey *script = [[BNMultisigScriptPubKey alloc] init];
    BNKey *key = [_wallet createKey];
    [script.pubKeys addObject:key.pubKey];
    [script.pubKeys addObject:key.pubKey]; //do it twice to properly estimate tx size for fees
    txOut.scriptPubKey = script;
}

- (void)configureForReleaseWithInputTx:(BNTx *)inputTx
{
    BNTxIn *txIn = [self newInput];
    txIn.previousOutIndex = [NSNumber numberWithInt:0];
    txIn.previousTxHash = inputTx.txHash;
    txIn.previousTxSerializedHex = inputTx.serializedHex;
}

- (void)configureForEscrowSpendingOutput:(BNTxOut *)utxo
{
    [self configureForEscrowWithValue:utxo.value];
    
    BNTxIn *txIn = [[BNTxIn alloc] init];
    [txIn configureFromTxOut:utxo];
    [_inputs addObject:txIn];
}

- (void)payToAddress:(NSString *)address value:(NSNumber *)value
{
    BNTxOut *txOut = [self newOutput];
    
    txOut.value = value;
    
    BNPayToAddressScriptPubKey *script = [[BNPayToAddressScriptPubKey alloc] init];
    script.address = address;
    txOut.scriptPubKey = script;
}

- (void)addInputsAndChange
{
    [self copySlotsFrom:[self sendToServer:@"addInputsAndChange"]];
}

- (void)emptyWallet
{
    [self copySlotsFrom:[self sendToServer:@"emptyWallet"]];
}

- (void)addPayToAddressOutputWithValue:(NSNumber *)value
{
    [self payToAddress:[_wallet createKey].address value:value];
}

- (void)subtractFee
{
    [self copySlotsFrom:[self sendToServer:@"subtractFee"]]; //TODO it should reset fees first?
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

- (BOOL)wasBroadcast
{
    return [[self.canonicalTx sendToServer:@"wasBroadcast"] boolValue];
}

- (BOOL)isConfirmed
{
    if ([self isCancelled])
    {
        return NO;
    }
    else
    {
        if (self.subsumingTx)
        {
            return [self.subsumingTx isConfirmed];
        }
        else
        {
            return self.confirmations.intValue >= self.wallet.requiredConfirmations.intValue;
        }
    }
}

- (BOOL)isCancelled
{
    if (self.subsumingTx)
    {
        return ![self.subsumingTx isEquivalentTo:self];
    }
    else
    {
        return NO;
    }
}

- (void)lockInputs
{
    [self sendToServer:@"lockInputs"];
}

- (void)unlockInputs
{
    [self sendToServer:@"unlockInputs"];
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

- (NSNumber *)isSentToSelf
{
    if (!_isSentToSelf)
    {
        self.isSentToSelf = [self sendToServer:@"isSentToSelf"];
    }
    return _isSentToSelf;
}

- (NSString *)txType
{
    if ([_txType isEqualToString:@"Unknown"])
    {
        _txType = [self sendToServer:@"getTxType"];
    }
    
    if (_txType == nil)
    {
        if (self.netValue.longLongValue < 0)
        {
            if ([self multisigOutput])
            {
                return @"Lock Escrow";
            }
            else if ([[self isSentToSelf] boolValue])
            {
                return @"Setup Escrow";
            }
            else
            {
                return @"Withdrawal";
            }
        }
        else if ([self multisigInput])
        {
            return @"Payment";
        }
        else
        {
            return @"Deposit";
        }

    }
    else
    {
        return _txType;
    }
}

- (void)setTxType:(NSString *)txType
{
    [self sendToServer:@"setTxType" withArg:txType];
    _txType = txType;
}


- (NSString *)description
{
    if ([_description isEqualToString:@"Unknown"])
    {
        _description = [self sendToServer:@"getDescription"];
    }
    
    return _description;
}

- (void)setDescription:(NSString *)description
{
    [self sendToServer:@"setDescription" withArg:description];
    _description = description;
}

- (NSDate *)updateTimeDate
{
    NSNumber *d = self.updateTime;
    
    if (d)
    {
        return [NSDate dateWithTimeIntervalSince1970:(NSTimeInterval)(d.doubleValue/1000.0)];
    }
    
    return nil;
}

- (NSString *)nodeSubtitle
{
    /*
    if (self.updateTimeDate)
    {
        return self.updateTimeDate.itemDateTimeString;
    }
    */
    
    return [NSString stringWithFormat:@"%@ - %@", self.updateTimeDate.itemDateTimeString, self.confirmStatus];
    //return self.confirmStatus;
    //return self.txHash;
}

- (NSString *)nodeNote
{
    if (self.isConfirmed)
    {
        return nil; //@"✓";
    }
    
    return @"●";
}

- (NSString *)nodeTitle
{
    /*
    if (self.children.count == 0)
    {
        [self composeChildrenFromPropertyNames:@[@"updateTime", @"counterParty"]];
    }
    */
 
    return [NSString stringWithFormat:@"%@ of %.4f BTC",
            self.txType,
            (float)(self.netValue.doubleValue * 0.00000001)];
    
/*
    return [NSString stringWithFormat:@"%@ of %.4f BTC (%@)",
            self.txTypeString,
            (float)(self.netValue.doubleValue * 0.00000001),
            self.isConfirmed ? [NSString stringWithFormat:@"%@ confirmations", self.confirmations] : @"pending"
    ];
*/
}

- (NSString *)confirmStatus
{
    return self.isConfirmed ? [NSString stringWithFormat:@"%@ confirms", self.confirmations] : @"pending";
}

- (NSUInteger)hash
{
    return [self.txHash hash];
}

- (BOOL)isEqual:(id)object
{
    if ([object isKindOfClass:BNTx.class])
    {
        BNTx *otherTx = object;
        return [self.txHash isEqualTo:[otherTx txHash]];
    }
    
    return NO;
}

- (BOOL)isEqualTo:(id)object
{
    return [self.txHash isEqualTo:[object performSelector:@selector(txHash)]];
}

- (NSString *)webUrl
{
    // http://testnet.helloblock.io/addresses/n1grcACynNZCB9zN1G4sHP9BETiFZrC15y
    return [@"http://testnet.helloblock.io/transactions/" stringByAppendingString:self.txHash];
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

- (BNTxOut *)firstOutput
{
    return (BNTxOut *)[self.outputs firstObject];
}

// actions

- (NSArray *)modelActions
{
    return @[@"inspect"];
}

- (void)inspect
{
    NSURL *url = [NSURL URLWithString:self.webUrl];
    [[NSWorkspace sharedWorkspace] openURL:url];
}

- (void)fetch
{
    if (self.subsumingTx)
    {
        [self.subsumingTx fetch];
    }
    else
    {
        self.subsumingTx = [self sendToServer:@"subsumingTx"];
        if (self.subsumingTx)
        {
            self.subsumingTx.wallet = self.wallet;
            self.subsumingTx.bnParent = self.bnParent;
        }
        self.confirmations = [self sendToServer:@"confirmations"];
    }
    
    
    [self postSelfChanged];
}

- (BOOL)isEquivalentTo:(BNTx *)otherTx
{
    return [self.inputs isEqualToArray:otherTx.inputs] && [self.outputs isEqualToArray:otherTx.outputs];
}

- (BNTx *)canonicalTx
{
    if (self.subsumingTx && [self.subsumingTx isEquivalentTo:self])
    {
        return self.subsumingTx;
    }
    else
    {
        return self;
    }
}

- (NSNumber *)ordinality
{
    return [NSNumber numberWithInt:-1*self.updateTime.intValue];
}

//for display in wallet
- (NSString *)updateTimeDescription
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.timeStyle = NSDateFormatterShortStyle;
    dateFormatter.dateStyle = NSDateFormatterShortStyle;
    dateFormatter.locale = [NSLocale systemLocale];
    dateFormatter.timeZone = [NSTimeZone systemTimeZone];
    return [dateFormatter stringFromDate:self.updateTimeDate];
}

@end
