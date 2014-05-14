//
//  NSArray+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSArray+BNJSON.h"
#import "NSObject+BNJSON.h"
#import "BNObject.h"

@implementation NSArray (BNJSON)

- asJSONObject
{
    NSMutableArray *array = [NSMutableArray array];
    for (id obj in self)
    {
        [array addObject:[obj asJSONObject]];
    }
    return array;
}

- (id)asObjectFromJSONObject
{
    NSMutableArray *array = [NSMutableArray array];
    for (id obj in self)
    {
        [array addObject:[obj asObjectFromJSONObject]];
    }
    return array;
}

- (void)setBnParent:(BNObject *)bnParent
{
    for (id obj in self)
    {
        if ([obj respondsToSelector:@selector(setBnParent:)])
        {
            [obj performSelector:@selector(setBnParent:) withObject:bnParent];
        }
    }
}

@end
