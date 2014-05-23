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
        [slot setVisibleName:@"To Address"];
    }

    {
        NavDataSlot *slot = [mirror newDataSlotWithName:@"amountInBtc"];
        [slot setVisibleName:@"Amount"];
        //[slot setSuffix:@"BTC"];
    }
    
    {
        NavActionSlot *slot = [mirror newActionSlotWithName:@"send"];
        [slot setVisibleName:@"Send"];
    }
    
    return self;
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
    
    //...
    
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
    BOOL isConfirmed = NO;
    
    if (isConfirmed)
    {
        self.status = @"confirmed";
    }
}

@end
