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
        [slot setUneditedValue:@"0.0"];
    }
    
    [self updateActions];
    
    return self;
}

- (void)updatedSlot:(NavSlot *)aNavSlot
{
    [self updateActions];
}

- (void)updateActions
{
    NavActionSlot *slot = [self.navMirror newActionSlotWithName:@"send"];
    [slot setVisibleName:@"Widthdrawl"];
    [slot setIsActive:self.navMirror.dataSlotsAreFilled];
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

- (void)send
{
    BNWallet *wallet = self.wallet;
    
    NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
    f.numberStyle = NSNumberFormatterDecimalStyle;
    NSNumber *value = [[f numberFromString:self.amountInBtc] btcToSatoshi];
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
