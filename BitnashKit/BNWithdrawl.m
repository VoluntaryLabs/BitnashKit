//
//  BNWithdrawl.m
//  BitnashKit
//
//  Created by Steve Dekorte on 5/23/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWithdrawl.h"
#import "BNWallet.h"

@implementation BNWithdrawl

- (id)init
{
    self = [super init];
    
    self.nodeTitle = @"Withdrawl";
    
    self.nodeViewClass = NavMirrorView.class;
    
    NavMirror *mirror = self.navMirror;
    
    {
        NavDataSlot *slot = [mirror newDataSlotWithName:@"toAddress"];
        [slot setVisibleName:@"Address"];
        [slot setUneditedValue:@"Enter Recipient Bitcoin Address"];
    }

    {
        NavDataSlot *slot = [mirror newDataSlotWithName:@"amountInBtc"];
        [slot setVisibleName:@"Amount"];
        [slot setValueSuffix:@"BTC"];
        [slot setUneditedValue:@"?"];
        [slot setFormatterClassName:@"BNPriceFormatter"];
    }
    
    [self updateActions];
    
    return self;
}

- (BOOL)amountInBtcSlotIsValid
{
    NavDataSlot *slot = [self.navMirror dataSlotNamed:@"amountInBtc"];
    return slot.numberValue.floatValue >= 0;
}

- (BOOL)toAddressSlotIsValid
{
    NavDataSlot *slot = [self.navMirror dataSlotNamed:@"toAddress"];
    //NSLog(@"self.toAddress = '%@'", slot.value);
    return [self.wallet isValidAddress:slot.value];
}

- (void)updatedSlot:(NavSlot *)aNavSlot
{
    [self updateActions];
}

- (BOOL)isReady
{
    return
        self.toAddressSlotIsValid &&
        self.amountInBtcSlotIsValid;
}

- (void)updateActions
{
    NavActionSlot *slot = [self.navMirror newActionSlotWithName:@"send"];
    [slot setVisibleName:@"Widthdraw"];
    [slot setIsActive:self.isReady];
    [slot.slotView syncFromSlot];
}

- (NSString *)nodeSubtitle
{
    return self.status;
}

- (BNWallet *)wallet
{
    return [self firstInParentChainOfClass:BNWallet.class];
}

- (NSNumber *)amountInSatoshi
{
    NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
    f.numberStyle = NSNumberFormatterDecimalStyle;
    NSNumber *value = [[f numberFromString:self.amountInBtc] btcToSatoshi];
    return value;
}

- (void)send
{
    BNWallet *wallet = self.wallet;
    NSNumber *value = self.amountInSatoshi;

    self.tx = [wallet newWithdrawalTxToAddress:self.toAddress withValue:value];
    self.tx.txType = @"Withdrawal";
    self.tx.description = self.toAddress;
    [self.tx sign];
    [self.tx broadcast];
    
    
    self.status = @"sending...";
    [self postSelfChanged];
}

- (void)update
{
    if ([self.status isEqualTo:@"sending.."])
    {
        [self checkForConfirm];
    }
}

- (void)checkForConfirm
{
    if (self.tx.isConfirmed)
    {
        self.status = @"confirmed";
    }
}

@end
